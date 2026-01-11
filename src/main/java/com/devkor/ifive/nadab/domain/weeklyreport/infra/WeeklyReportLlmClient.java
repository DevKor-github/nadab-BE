package com.devkor.ifive.nadab.domain.weeklyreport.infra;

import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.AiWeeklyReportResultDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.LlmWeeklyResultDto;
import com.devkor.ifive.nadab.global.core.prompt.weekly.WeeklyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
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
                .model(OpenAiApi.ChatModel.GPT_5_MINI)
                .reasoningEffort("medium")
                .temperature(1.0)
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
            LlmWeeklyResultDto result = objectMapper.readValue(content, LlmWeeklyResultDto.class);

            String discovered = result.discovered();
            String good = result.good();
            String improve = result.improve();

            if (isBlank(discovered) || isBlank(good) || isBlank(improve)) {
                throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }

            return new AiWeeklyReportResultDto(discovered, good, improve);

        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
