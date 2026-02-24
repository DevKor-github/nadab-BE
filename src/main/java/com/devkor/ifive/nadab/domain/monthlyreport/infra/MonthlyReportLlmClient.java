package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.global.core.prompt.monthly.MonthlyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
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
public class MonthlyReportLlmClient {

    private final MonthlyReportPromptLoader monthlyReportPromptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.GEMINI;
    private static final LlmProvider REWRITE_PROVIDER = LlmProvider.CLAUDE;

    private static final int MAX_DISCOVERED = 220;
    private static final int MAX_IMPROVE = 120;
    private static final int MIN_DISCOVERED = 140;
    private static final int MIN_IMPROVE = 60;

    private static final int MAX_HL_DISCOVERED = 2;
    private static final int MAX_HL_IMPROVE = 1;
    private static final int MAX_HL_SEG_LEN = 30;

    public AiReportResultDto generate(
            String monthStartDate, String monthEndDate, String weeklySummaries, String representativeEntries) {
        String prompt = monthlyReportPromptLoader.loadPrompt()
                .replace("{monthStartDate}", monthStartDate)
                .replace("{monthEndDate}", monthEndDate)
                .replace("{weeklySummaries}", weeklySummaries)
                .replace("{representativeEntries}", representativeEntries == null ? "" : representativeEntries);

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
            LlmResultDto result;
            try {
                result = objectMapper.readValue(content, LlmResultDto.class);
            } catch (Exception e) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MAPPING_FAILED);
            }

            StyledText discoveredStyled = result.discovered();
            StyledText improveStyled = result.improve();

            if (discoveredStyled == null || improveStyled == null) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MISSING_FIELDS);
            }

            validateStyledText(discoveredStyled, true);
            validateStyledText(improveStyled, false);

            ReportContent reportContent = new ReportContent(discoveredStyled, improveStyled);

            String discovered = reportContent.discovered().plainText();
            String improve = reportContent.improve().plainText();

            if (isBlank(discovered) || isBlank(improve)) {
                throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MISSING_FIELDS);
            }

            AiReportResultDto dto = enforceLength(new AiReportResultDto(reportContent, discovered, improve));

            validateLength(dto);

            return dto;

        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String callOpenAi(ChatClient client, String prompt) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_5_MINI)
                .reasoningEffort("medium")
                .temperature(1.0)
                .build();

        return client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();
    }

    private String callClaude(ChatClient client, String prompt) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(AnthropicApi.ChatModel.CLAUDE_3_HAIKU)
                .temperature(0.3)
                .build();

        return client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();
    }

    private String callGemini(ChatClient client, String prompt) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                .responseMimeType("application/json")
                .temperature(0.3)
                .build();

        return client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();
    }

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

        ReportContent newContent = new ReportContent(d, i);
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
            """.formatted(min, max, maxHl, jsonInput);

            GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                    .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                    .responseMimeType("application/json")
                    .temperature(0.0)
                    .build();

            String out = client.prompt().user(prompt).options(options).call().content();

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

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void validateStyledText(StyledText st, boolean isDiscovered) {
        if (st.segments() == null || st.segments().isEmpty()) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_AI_JSON_MISSING_FIELDS);
        }

        int highlightCount = 0;

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
                highlightCount++;
                if (t.length() > MAX_HL_SEG_LEN) {
                    throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_HIGHLIGHT_SEGMENT_TOO_LONG);
                }
            }
        }

        int maxHl = isDiscovered ? MAX_HL_DISCOVERED : MAX_HL_IMPROVE;
        if (highlightCount > maxHl) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_HIGHLIGHT_COUNT_EXCEEDED);
        }
    }

    private void validateLength(AiReportResultDto dto) {
        int dLen = dto.discovered().length();
        int iLen = dto.improve().length();

        if (dLen < MIN_DISCOVERED || dLen > MAX_DISCOVERED) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_DISCOVERED_LENGTH_INVALID);
        }
        if (iLen < MIN_IMPROVE || iLen > MAX_IMPROVE) {
            throw new AiResponseParseException(ErrorCode.MONTHLY_REPORT_IMPROVE_LENGTH_INVALID);
        }
    }
}