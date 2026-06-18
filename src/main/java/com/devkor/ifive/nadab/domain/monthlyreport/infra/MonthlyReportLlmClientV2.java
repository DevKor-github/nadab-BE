package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.AiMonthlyReportResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.typereport.application.helper.TypeReportInputAssembler;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.devkor.ifive.nadab.global.core.prompt.monthly.MonthlyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmExceptionMapper;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.shared.reportcontent.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyReportLlmClientV2 {

    private final MonthlyReportPromptLoader monthlyReportPromptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.GEMINI;
    private static final LlmProvider REWRITE_PROVIDER = LlmProvider.GEMINI;

    private static final int MAX_DISCOVERED = 400;
    private static final int MIN_DISCOVERED = 150;

    private static final int MAX_EMOTION = 200;
    private static final int MIN_EMOTION = 50;

    private static final int MIN_SUMMARY = 8;
    private static final int MAX_SUMMARY = 30;

    private static final int MIN_COMPARISON_EMOTION_TREND = 20;
    private static final int MAX_COMPARISON_EMOTION_TREND = 55;
    private static final int MAX_COMPARISON_EMOTION = 100;
    private static final int MAX_COMPARISON_BOLD_SEGMENTS = 3;

    public AiMonthlyReportResultDto generate(
            String monthStartDate,
            String monthEndDate,
            String weeklySummaries,
            String representativeEntries,
            TypeEmotionStatsContent emotionStats,
            MonthlyReportComparisonInputDto comparisonInput) {
        String prompt = buildPrompt(
                monthStartDate,
                monthEndDate,
                weeklySummaries,
                representativeEntries,
                emotionStats,
                comparisonInput
        );

        ChatClient client = llmRouter.route(provider);

        String content = switch (provider) {
            case OPENAI -> callOpenAi(client, prompt);
            case CLAUDE -> callClaude(client, prompt);
            case GEMINI -> callGemini(client, prompt);
        };

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            AiMonthlyReportResultDto result;
            try {
                result = objectMapper.readValue(content, AiMonthlyReportResultDto.class);
            } catch (Exception e) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MAPPING_FAILED);
            }

            MonthlyReportV2Content monthlyReportContent = result.content();

            StyledText discoveredStyled = monthlyReportContent.discovered();
            StyledText commentStyled = monthlyReportContent.comment();
            String summary = monthlyReportContent.summary();
            String commentSummary = monthlyReportContent.commentSummary();
            String dominantKeyword = monthlyReportContent.dominantKeyword();
            String emotionTrend = monthlyReportContent.emotionTrend();

            TypeTextContent emotionStatsContent = result.emotionSummaryContent();
            StyledText styledText = emotionStatsContent.styledText();

            if (discoveredStyled == null || commentStyled == null || styledText == null
                    || isBlank(summary) || isBlank(commentSummary) || isBlank(dominantKeyword) || isBlank(emotionTrend)) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MISSING_FIELDS);
            }

            validateSummary(summary);
            validateSummary(commentSummary);

            validateStyledText(discoveredStyled, true);
            validateStyledText(commentStyled, true);
            validateStyledText(styledText, false);

            String discovered = monthlyReportContent.discovered().plainText();
            String comment = monthlyReportContent.comment().plainText();
            String emotion = emotionStatsContent.styledText().plainText();

            if (isBlank(discovered) || isBlank(comment)  || isBlank(emotion)) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MISSING_FIELDS);
            }

            validateLength(discovered, comment, emotion, comparisonInput != null);

            if (comparisonInput != null) {
                validateComparisonEmotionTrend(emotionTrend, dominantKeyword);
                validateComparisonEmotionSummary(styledText, dominantKeyword);
            }

            return result;

        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    String buildPrompt(
            String monthStartDate,
            String monthEndDate,
            String weeklySummaries,
            String representativeEntries,
            TypeEmotionStatsContent emotionStats,
            MonthlyReportComparisonInputDto comparisonInput
    ) {
        String template = comparisonInput == null
                ? monthlyReportPromptLoader.loadV2BaselinePrompt()
                : monthlyReportPromptLoader.loadV2ComparisonPrompt();

        String prompt = template
                .replace("{monthStartDate}", monthStartDate)
                .replace("{monthEndDate}", monthEndDate)
                .replace("{weeklySummaries}", weeklySummaries)
                .replace("{representativeEntries}", representativeEntries == null ? "" : representativeEntries)
                .replace("{emotionStats}", TypeReportInputAssembler.assembleEmotionStats(emotionStats));

        if (comparisonInput != null) {
            prompt = prompt.replace("{comparisonInput}", serializeComparisonInput(comparisonInput));
        }

        return prompt;
    }

    private String serializeComparisonInput(MonthlyReportComparisonInputDto comparisonInput) {
        try {
            return objectMapper.writeValueAsString(comparisonInput);
        } catch (JsonProcessingException e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private String callOpenAi(ChatClient client, String prompt) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_5_MINI)
                .reasoningEffort("medium")
                .temperature(1.0)
                .build();

        try {
            return client.prompt()
                    .user(prompt)
                    .options(options)
                    .call()
                    .content();
        } catch (Exception e) {
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        }
    }

    private String callClaude(ChatClient client, String prompt) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(AnthropicApi.ChatModel.CLAUDE_3_HAIKU)
                .temperature(0.3)
                .build();

        try {
            return client.prompt()
                    .user(prompt)
                    .options(options)
                    .call()
                    .content();
        } catch (Exception e) {
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        }
    }

    private String callGemini(ChatClient client, String prompt) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                .responseMimeType("application/json")
                .temperature(0.3)
                .build();

        try {
            return client.prompt()
                    .user(prompt)
                    .options(options)
                    .call()
                    .content();
        } catch (Exception e) {
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        }
    }

    /*
    private AiReportResultDto enforceLength(AiReportResultDto dto) {
        ReportContent c = dto.content();

        int dLen = dto.discovered().length();
        int iLen = dto.improve().length();

        boolean badD = dLen < MIN_DISCOVERED || dLen > MAX_DISCOVERED;
        boolean badI = iLen < MIN_IMPROVE || iLen > MAX_IMPROVE;

        if (!badD && !badI) return dto;

        ChatClient rewriteClient = llmRouter.route(REWRITE_PROVIDER);

        StyledText d = c.discovered();
        StyledText i = c.improve();

        if (badD) d = rewriteStyled(rewriteClient, d, true);
        if (badI) i = rewriteStyled(rewriteClient, i, false);

        ReportContent newContent = new ReportContent(c.summary(), d, i);
        String newD = newContent.discovered().plainText();
        String newI = newContent.improve().plainText();

        try {
            validateStyledText(newContent.discovered(), true);
            validateStyledText(newContent.improve(), false);
        } catch (AiResponseParseException e) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_REWRITE_FORMAT_INVALID);
        }

        return new AiReportResultDto(newContent, newD, newI);
    }

    private StyledText rewriteStyled(ChatClient client, StyledText in, boolean isDiscovered) {
        int min = isDiscovered ? MIN_DISCOVERED : MIN_IMPROVE;
        int max = isDiscovered ? MAX_DISCOVERED : MAX_IMPROVE;
        int maxHl = isDiscovered ? MAX_HL_DISCOVERED : MAX_HL_IMPROVE;

        try {
            String jsonInput = objectMapper.writeValueAsString(in);

            String prompt = """
            아래 JSON의 segments[].text를 이어 붙인 '순수 텍스트'의 의미는 유지하면서,
            글자수(공백 포함)를 최소 %d자 ~ 최대 %d자로 맞춰 같은 구조로 다시 만들어 주세요.

            [반드시 지킬 것]
            - 출력은 JSON 1개만: {"segments":[{"text":"...","marks":[...]} ... ]}
            - marks는 "BOLD", "HIGHLIGHT"만 사용
            - "HIGHLIGHT"가 있으면 같은 segment에 "BOLD"도 반드시 포함
            - HIGHLIGHT segment 개수는 최대 %d개
            - 줄바꿈 금지(\\n, \\r 금지)
            - 숫자/시간/빈도 표현 금지
            - 해요체 유지
            - 문학적 비유/감정 과잉/훈수/응원 나열 금지
            - 세그먼트는 3~8개 정도로 자연스럽게 구성

            [입력 JSON]
            %s
            """.formatted(min + 10, max - 10, maxHl, jsonInput);

            GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                    .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                    .responseMimeType("application/json")
                    .temperature(0.0)
                    .build();

            String out;
            try {
                out = client.prompt().user(prompt).options(options).call().content();
            } catch (Exception e) {
                throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_REWRITE_NO_RESPONSE, e);
            }

            if (out == null || out.isBlank()) {
                throw new AiServiceUnavailableException(ErrorCode.AI_REWRITE_NO_RESPONSE);
            }

            try {
                return objectMapper.readValue(out, StyledText.class);
            } catch (Exception e) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_REWRITE_JSON_MAPPING_FAILED);
            }

        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new AiResponseParseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

     */

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void validateStyledText(StyledText st, boolean isDiscovered) {
        if (st.segments() == null || st.segments().isEmpty()) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MISSING_FIELDS);
        }

        for (Segment seg : st.segments()) {
            if (seg == null || seg.text() == null || seg.text().isBlank()) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_SEGMENT_INVALID);
            }
            String t = seg.text();
            if (t.isBlank()) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_SEGMENT_INVALID);
            }
            if (t.contains("\n") || t.contains("\r")) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_SEGMENT_INVALID);
            }

            List<Mark> marks = seg.marks() == null ? List.of() : seg.marks();
            EnumSet<Mark> set = EnumSet.noneOf(Mark.class);
            for (Mark m : marks) {
                if (m == null) {
                    throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_SEGMENT_INVALID);
                }
                set.add(m);
            }

            if (set.contains(Mark.HIGHLIGHT)) {
                if (!set.contains(Mark.BOLD)) {
                    throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_HIGHLIGHT_WITHOUT_BOLD);
                }
            }
        }
    }

    void validateLength(String discovered, String comment, String emotion, boolean comparison) {
        int dLen = discovered.length();
        int cLen = comment.length();
        int eLen = emotion.length();

        if (dLen < MIN_DISCOVERED || dLen > MAX_DISCOVERED) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_DISCOVERED_LENGTH_INVALID);
        }
        if (cLen < MIN_DISCOVERED || cLen > MAX_DISCOVERED) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_DISCOVERED_LENGTH_INVALID);
        }
        int maxEmotion = comparison ? MAX_COMPARISON_EMOTION : MAX_EMOTION;
        if (eLen < MIN_EMOTION || eLen > maxEmotion) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_IMPROVE_LENGTH_INVALID);
        }
    }

    void validateComparisonEmotionTrend(String emotionTrend, String dominantKeyword) {
        String trend = emotionTrend.trim();
        if (trend.length() < MIN_COMPARISON_EMOTION_TREND
                || trend.length() > MAX_COMPARISON_EMOTION_TREND
                || trend.contains("\n")
                || trend.contains("\r")
                || containsDigit(trend)
                || countOccurrences(trend, dominantKeyword) != 1) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_SUMMARY_INVALID);
        }
    }

    void validateComparisonEmotionSummary(StyledText emotionSummary, String dominantKeyword) {
        int boldSegments = 0;

        for (Segment segment : emotionSummary.segments()) {
            List<Mark> marks = segment.marks() == null ? List.of() : segment.marks();
            if (marks.contains(Mark.HIGHLIGHT)) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_SEGMENT_INVALID);
            }
            if (marks.contains(Mark.BOLD)) {
                boldSegments++;
            }
        }

        if (boldSegments == 0
                || boldSegments > MAX_COMPARISON_BOLD_SEGMENTS
                || countOccurrences(emotionSummary.plainText(), dominantKeyword) != 1) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_SEGMENT_INVALID);
        }
    }

    private boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private int countOccurrences(String value, String target) {
        if (isBlank(value) || isBlank(target)) {
            return 0;
        }

        int count = 0;
        int fromIndex = 0;
        while ((fromIndex = value.indexOf(target, fromIndex)) >= 0) {
            count++;
            fromIndex += target.length();
        }
        return count;
    }

    private void validateSummary(String summary) {
        if (summary == null) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_SUMMARY_INVALID);
        }
        String s = summary.trim();
        if (s.length() < MIN_SUMMARY || s.length() > MAX_SUMMARY) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_SUMMARY_INVALID);
        }
        // 문장부호로 끝내지 않기
        if (s.endsWith(".") || s.endsWith("!") || s.endsWith("?")) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_SUMMARY_INVALID);
        }
        // 따옴표 금지(프론트가 처리)
        if (s.contains("\"") || s.contains("'")) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_SUMMARY_INVALID);
        }
        // 숫자 금지(시간/빈도 방지)
        if (containsDigit(s)) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_SUMMARY_INVALID);
        }
    }
}
