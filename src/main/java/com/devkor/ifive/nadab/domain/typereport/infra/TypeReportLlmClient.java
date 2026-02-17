package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.global.core.prompt.type.report.TypeReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypeReportLlmClient {

    private final TypeReportPromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.GEMINI;
    private static final LlmProvider REWRITE_PROVIDER = LlmProvider.CLAUDE;

    public JsonNode generateRaw(String selectedType, String patterns, String evidenceCards) {
        if (blank(selectedType) || blank(patterns) || blank(evidenceCards)) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_GENERATE_INPUT_EMPTY);
        }

        String prompt = promptLoader.loadPrompt()
                .replace("{selectedType}", selectedType)
                .replace("{patterns}", patterns)
                .replace("{evidenceCards}", evidenceCards);

        ChatClient client = llmRouter.route(provider);

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                .responseMimeType("application/json")
                .temperature(0.3)
                .build();

        String content = client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            return objectMapper.readTree(content);
        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    public JsonNode repairRaw(String analysisTypeCode, String rawJson) {
        if (blank(analysisTypeCode) || blank(rawJson)) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_GENERATE_INPUT_EMPTY);
        }

        String prompt = promptLoader.loadRepairPrompt()
                .replace("{analysisTypeCode}", analysisTypeCode)
                .replace("{rawJson}", rawJson);

        ChatClient client = llmRouter.route(REWRITE_PROVIDER);

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(AnthropicApi.ChatModel.CLAUDE_3_HAIKU)
                .temperature(0.3)
                .build();

        String content = client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            return objectMapper.readTree(content);
        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
