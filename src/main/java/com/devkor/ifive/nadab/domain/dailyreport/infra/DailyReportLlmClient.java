package com.devkor.ifive.nadab.domain.dailyreport.infra;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.LlmDailyResultDto;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReportLlmClient {

    private final DailyReportPromptLoader dailyReportPromptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.OPENAI;

    public AiDailyReportResultDto
    generate(String question, String answer) {
        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        ChatClient chatClient = llmRouter.route(provider);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                .temperature(0.3)
                .maxTokens(512)
                .build();

        String content = chatClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            // 3. JSON → DTO 역직렬화
            LlmDailyResultDto result = objectMapper.readValue(content, LlmDailyResultDto.class);

            String message = result.message();

            String emotion = result.emotion();

            if (isBlank(message) || isBlank(emotion)) {
                throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }

            return new AiDailyReportResultDto(
                    message,
                    emotion
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
