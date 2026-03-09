package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.global.core.prompt.type.select.TypeSelectPromptLoader;
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
public class TypeSelectLlmClient {

    private final TypeSelectPromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.OPENAI;

    public JsonNode selectTypeRawJson(String candidatesText, String patternsText) {
        if (candidatesText == null || candidatesText.isBlank() || patternsText == null || patternsText.isBlank()) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_INPUT_EMPTY);
        }

        String prompt = promptLoader.loadPrompt()
                .replace("{candidates}", candidatesText)
                .replace("{patterns}", patternsText);

        ChatClient client = llmRouter.route(provider);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                .temperature(0.0)
                .build();

        String content = client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_TYPE_SELECT_NO_RESPONSE);
        }

        try {
            return objectMapper.readTree(content);
        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_MISSING_FIELDS);
        }
    }
}
