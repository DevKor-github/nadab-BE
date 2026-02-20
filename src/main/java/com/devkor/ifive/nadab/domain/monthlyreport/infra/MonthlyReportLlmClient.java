package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.AiMonthlyReportResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.LlmMonthlyResultDto;
import com.devkor.ifive.nadab.global.core.prompt.monthly.MonthlyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
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

@Component
@RequiredArgsConstructor
public class MonthlyReportLlmClient {

    private final MonthlyReportPromptLoader monthlyReportPromptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.GEMINI;
    private static final LlmProvider REWRITE_PROVIDER = LlmProvider.CLAUDE;

    private static final int MAX_DISCOVERED = 210;
    private static final int MAX_IMPROVE = 110;
    private static final int MIN_DISCOVERED = 160;
    private static final int MIN_IMPROVE = 80;

    public AiMonthlyReportResultDto generate(
            String monthStartDate, String monthEndDate, String weeklySummaries, String representativeEntries) {
        String prompt = monthlyReportPromptLoader.loadPrompt()
                .replace("{monthStartDate}", monthStartDate)
                .replace("{monthEndDate}", monthEndDate)
                .replace("{weeklySummaries}", weeklySummaries)
                .replace("{representativeEntries}", representativeEntries);

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
            LlmMonthlyResultDto result = objectMapper.readValue(content, LlmMonthlyResultDto.class);

            String discovered = result.discovered();
            String improve = result.improve();

            if (isBlank(discovered) || isBlank(improve)) {
                throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }

            AiMonthlyReportResultDto dto = this.enforceLength(new AiMonthlyReportResultDto(discovered, improve));
            return dto;

        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
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

    private AiMonthlyReportResultDto enforceLength(AiMonthlyReportResultDto dto) {
        String d = dto.discovered();
        String i = dto.improve();

        boolean needD = d.length() > MAX_DISCOVERED;
        boolean needI = i.length() > MAX_IMPROVE;

        if (!needD && !needI) return dto;

        ChatClient rewriteClient = llmRouter.route(REWRITE_PROVIDER);

        if (needD) d = rewriteOne(rewriteClient, d, MAX_DISCOVERED, MIN_DISCOVERED);
        if (needI) i = rewriteOne(rewriteClient, i, MAX_IMPROVE, MIN_IMPROVE);

        return new AiMonthlyReportResultDto(d, i);
    }

    private String rewriteOne(ChatClient client, String text, int maxChars, int minChars) {
        String prompt = """
    아래 텍스트를 의미는 유지하되 최소 %d자 ~ 최대 %d자(공백 포함)로 줄여주세요.
    
    [필수 규칙]
    - **출력 말투는 반드시 해요체로 통일하세요.**
      - 입력이 반말/해체/명령형/거친 표현이어도, 정중한 해요체로 **말투를 교정**해서 출력하세요.
      - 예: "했어", "해봐", "괜찮아" -> "했어요", "해보는 건 어때요?", "괜찮아요"
    - 해요체 유지 (문장 끝: ~해요/~했어요/~이에요/~예요)
    - 문학적 비유/감정 과잉/훈수/응원 나열 금지
    - 결과는 JSON 1개만 출력: {"text":"..."}
    - 반드시 %d자를 넘기지 마세요.

    [텍스트]
    %s
    """.formatted(minChars, maxChars, maxChars, text);

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(AnthropicApi.ChatModel.CLAUDE_3_HAIKU)
                .temperature(0.0)
                .build();

        String content = client.prompt().user(prompt).options(options).call().content();

        try {
            var node = objectMapper.readTree(content);
            String out = node.get("text").asText();
            if (out == null || out.isBlank()) throw new RuntimeException();
            return out;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}