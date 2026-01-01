package com.devkor.ifive.nadab.domain.dailyreport.infra;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.LlmResultDto;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReportLlmClient {

    private final ChatClient chatClient;
    private final DailyReportPromptLoader dailyReportPromptLoader;
    private final ObjectMapper objectMapper;

    public AiReportResultDto generate(String question, String answer) {
        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.3)
                .maxTokens(512)
                .build();

        String content = chatClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException("AI 서비스로부터 응답을 받지 못했습니다.");
        }

        try {
            // 3. JSON → DTO 역직렬화
            LlmResultDto result = objectMapper.readValue(content, LlmResultDto.class);

            String message = result.message();
            String emotion = result.emotion();

            return new AiReportResultDto(
                    message,
                    emotion
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException("AI 응답 형식을 해석할 수 없습니다.");
        }
    }

}
