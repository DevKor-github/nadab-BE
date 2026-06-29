package com.devkor.ifive.nadab.global.core.prompt.monthly;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev","prod"})
@Slf4j
@RequiredArgsConstructor
public class SecretMonthlyReportPromptLoader implements MonthlyReportPromptLoader {

    @Value("${MONTHLY_V1_PROMPT}")
    private String monthlyV1Prompt;

    @Value("${MONTHLY_V2_BASELINE_PROMPT}")
    private String monthlyV2BaselinePrompt;

    @Value("${MONTHLY_V2_COMPARISON_PROMPT}")
    private String monthlyV2ComparisonPrompt;

    @Override
    public String loadV1Prompt() {
        if (monthlyV1Prompt == null ||  monthlyV1Prompt.isBlank()) {
            log.error("환경 변수 MONTHLY_V1_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_ENV_VAR_NOT_SET);
        }

        return monthlyV1Prompt;
    }

    @Override
    public String loadV2BaselinePrompt() {
        if (monthlyV2BaselinePrompt == null || monthlyV2BaselinePrompt.isBlank()) {
            log.error("환경 변수 MONTHLY_V2_BASELINE_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_ENV_VAR_NOT_SET);
        }

        return monthlyV2BaselinePrompt;
    }

    @Override
    public String loadV2ComparisonPrompt() {
        if (monthlyV2ComparisonPrompt == null || monthlyV2ComparisonPrompt.isBlank()) {
            log.error("환경 변수 MONTHLY_V2_COMPARISON_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_ENV_VAR_NOT_SET);
        }

        return monthlyV2ComparisonPrompt;
    }

}
