package com.devkor.ifive.nadab.global.core.prompt.type.report;

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
public class SecretTypeReportPromptLoader implements TypeReportPromptLoader {

    @Value("${TYPE_PROMPT}")
    private String rawPrompt;

    @Value("${REPAIR_TYPE_PROMPT}")
    private String rawRepairPrompt;

    @Override
    public String loadPrompt() {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            log.error("환경 변수 TYPE_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_TYPE_ENV_VAR_NOT_SET);
        }

        return rawPrompt;
    }

    @Override
    public String loadRepairPrompt() {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            log.error("환경 변수 REPAIR_TYPE_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_TYPE_REPAIR_ENV_VAR_NOT_SET);
        }

        return rawPrompt;
    }
}
