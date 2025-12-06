package com.devkor.ifive.nadab.domain.report.application;

import com.devkor.ifive.nadab.domain.report.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.report.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.report.core.dto.AiReportResultDto;
import com.devkor.ifive.nadab.global.core.prompt.DailyReportPromptLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
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
            return new DailyReportResponse(
                    "죄송합니다. 응답을 생성할 수 없습니다.",
                    "기타",
                    0
            );
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
            return new DailyReportResponse(
                    "AI 응답 형식을 해석할 수 없어 기본 메시지를 반환합니다.",
                    "기타",
                    0
            );
        }
    }
}
