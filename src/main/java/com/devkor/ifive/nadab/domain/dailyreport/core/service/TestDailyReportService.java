package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.TestDailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.TestDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestDailyReportService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final DailyReportPromptLoader dailyReportPromptLoader;

    @Transactional
    public TestDailyReportResponse generateTestDailyReport(TestDailyReportRequest request) {
        String question = request.question();
        String answer = request.answer();
        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.3)
                .maxTokens(512)
                .build();

        // ChatClient를 통해 GPT API 호출
        String response = chatClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (response == null || response.trim().isEmpty()) {
            throw new AiServiceUnavailableException("AI 서비스로부터 응답을 받지 못했습니다.");
        }

        try {
            // 3. JSON → DTO 역직렬화
            AiDailyReportResultDto result = objectMapper.readValue(response, AiDailyReportResultDto.class);

            String message = result.message();
            String emotion = result.emotion();

            return new TestDailyReportResponse(
                    message,
                    emotion,
                    message.length()
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException("AI 응답 형식을 해석할 수 없습니다.");
        }
    }
}
