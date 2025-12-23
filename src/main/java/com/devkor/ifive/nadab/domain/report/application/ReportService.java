package com.devkor.ifive.nadab.domain.report.application;

import com.devkor.ifive.nadab.domain.report.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.report.api.dto.request.TestDailyReportRequest;
import com.devkor.ifive.nadab.domain.report.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.report.core.dto.AiReportResultDto;
import com.devkor.ifive.nadab.global.core.prompt.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.exception.AiServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ChatClient chatClient;
    private final DailyReportPromptLoader dailyReportPromptLoader;
    private final ObjectMapper objectMapper;

    public DailyReportResponse generateDailyReport(DailyReportRequest request) {
        String question = request.question();
        String answer = request.answer();

        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        // ChatClient를 통해 GPT API 호출
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        if (response == null || response.trim().isEmpty()) {
            throw new AiServiceException("AI 서비스로부터 응답을 받지 못했습니다.");
        }

        try {
            // 3. JSON → DTO 역직렬화
            AiReportResultDto result = objectMapper.readValue(response, AiReportResultDto.class);

            String message = result.message();
            String emotion = result.emotion();

            return new DailyReportResponse(
                    message,
                    emotion,
                    message.length()
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiServiceException("AI 응답 형식을 해석할 수 없습니다.");
        }
    }

    public DailyReportResponse generateTestDailyReport(TestDailyReportRequest request, String promptInput) {
        String question = request.question();
        String answer = request.answer();
        String prompt = promptInput
                .replace("{question}", question)
                .replace("{answer}", answer);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(
                        request.temperature() != null ? request.temperature() : 0.0
                )
                .maxTokens(512)
                .build();

        // ChatClient를 통해 GPT API 호출
        String response = chatClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (response == null || response.trim().isEmpty()) {
            throw new AiServiceException("AI 서비스로부터 응답을 받지 못했습니다.");
        }

        try {
            // 3. JSON → DTO 역직렬화
            AiReportResultDto result = objectMapper.readValue(response, AiReportResultDto.class);

            String message = result.message();
            String emotion = result.emotion();

            return new DailyReportResponse(
                    message,
                    emotion,
                    message.length()
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiServiceException("AI 응답 형식을 해석할 수 없습니다.");
        }
    }
}
