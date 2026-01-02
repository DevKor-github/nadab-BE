package com.devkor.ifive.nadab.domain.weeklyreport.core.infra;

import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.AiWeeklyReportResultDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.LlmWeeklyResultDto;
import com.devkor.ifive.nadab.global.core.prompt.weekly.WeeklyReportPromptLoader;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyReportLlmClient {

    private final ChatClient chatClient;
    private final WeeklyReportPromptLoader weeklyReportPromptLoader;
    private final ObjectMapper objectMapper;

    /**
     * @param weekStartDate 예: 2026-01-01
     * @param weekEndDate   예: 2026-01-07
     * @param entries       WeeklyEntriesAssembler 결과 문자열
     */
    public AiWeeklyReportResultDto generate(String weekStartDate, String weekEndDate, String entries) {
        String prompt = weeklyReportPromptLoader.loadPrompt()
                .replace("{weekStartDate}", weekStartDate)
                .replace("{weekEndDate}", weekEndDate)
                .replace("{entries}", entries);

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
            LlmWeeklyResultDto result = objectMapper.readValue(content, LlmWeeklyResultDto.class);

            String discovered = result.discovered();
            String good = result.good();
            String improve = result.improve();

            if (isBlank(discovered) || isBlank(good) || isBlank(improve)) {
                throw new AiResponseParseException("AI 응답 JSON의 필수 필드가 비어있습니다.");
            }

            return new AiWeeklyReportResultDto(discovered, good, improve);

        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException("AI 응답 형식을 해석할 수 없습니다.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
