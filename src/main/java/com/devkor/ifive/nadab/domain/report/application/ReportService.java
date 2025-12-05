package com.devkor.ifive.nadab.domain.report.application;

import com.devkor.ifive.nadab.domain.report.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.report.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.global.core.prompt.DailyReportPromptLoader;
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

        // 응답이 없을 경우 기본 메시지 반환
        if (response == null || response.trim().isEmpty()) {
            response = "죄송합니다. 응답을 생성할 수 없습니다.";
        }

        // 응답 길이 계산
        int responseLength = response.length();

        return new DailyReportResponse(response, responseLength);
    }
}
