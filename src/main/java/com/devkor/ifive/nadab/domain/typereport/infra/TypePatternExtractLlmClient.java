package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.global.core.prompt.type.pattern.TypePatternExtractPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypePatternExtractLlmClient {

    private final TypePatternExtractPromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.OPENAI;

    public JsonNode extractPatternsRawJson(String cardsText) {
        String prompt = promptLoader.loadPrompt().replace("{cards}", cardsText);

        ChatClient client = llmRouter.route(provider);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_5_MINI)
                .reasoningEffort("medium")
                .temperature(1.0)
                .build();

        String content = client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_PATTERN_EXTRACT_NO_RESPONSE);
        }

        try {
            return objectMapper.readTree(content);
        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            // 파싱 실패 / LLM 이상 응답
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }
}
