package com.devkor.ifive.nadab.global.core.prompt.daily;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Slf4j
@RequiredArgsConstructor
public class SecretDailyReportPromptLoader implements DailyReportPromptLoader{

    @Value("${INSIGHT_PROMPT}")
    private String rawPrompt;

    @Override
    public String loadPrompt() {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            log.error("환경 변수 INSIGHT_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_DAILY_ENV_VAR_NOT_SET);
        }

        return rawPrompt;
    }
}
