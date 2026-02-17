package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.global.core.prompt.type.evidence.TypeEvidenceCardPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TypeEvidenceCardLlmClient {

    private final ChatClient chatClient;
    private final TypeEvidenceCardPromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public List<Map<String, String>> generateRawCardsJsonArray(String entriesText) {
        String prompt = promptLoader.loadPrompt()
                .replace("{entries}", entriesText);

        try {
            String content = chatClient.prompt()
                    .user(prompt)
                    .options(OpenAiChatOptions.builder()
                            .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                            .temperature(0.2)
                            .build())
                    .call()
                    .content();

            // JSON 배열 파싱: [{"id":"D1","card":"..."}...]
            return objectMapper.readValue(content, new TypeReference<>() {});
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            // 파싱 실패 / LLM 이상 응답
            throw new AiResponseParseException(ErrorCode.AI_EVIDENCE_CARD_NO_RESPONSE);
        }
    }
}
