package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.AiMonthlyReportResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.LlmMonthlyResultDto;
import com.devkor.ifive.nadab.global.core.prompt.monthly.MonthlyReportPromptLoader;
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
public class MonthlyReportLlmClient {

    private final MonthlyReportPromptLoader monthlyReportPromptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.OPENAI;

    public AiMonthlyReportResultDto generate(
            String monthStartDate, String monthEndDate, String weeklySummaries, String representativeEntries) {
        String prompt = monthlyReportPromptLoader.loadPrompt()
                .replace("{monthStartDate}", monthStartDate)
                .replace("{monthEndDate}", monthEndDate)
                .replace("{weeklySummaries}", weeklySummaries)
                .replace("{representativeEntries}", representativeEntries);

        ChatClient chatClient = llmRouter.route(provider);

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
            LlmMonthlyResultDto result = objectMapper.readValue(content, LlmMonthlyResultDto.class);

            String discovered = result.discovered();
            String good = result.good();
            String improve = result.improve();

            if (isBlank(discovered) || isBlank(good) || isBlank(improve)) {
                throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }

            return new AiMonthlyReportResultDto(discovered, good, improve);

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