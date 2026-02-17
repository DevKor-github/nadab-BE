package com.devkor.ifive.nadab.global.core.prompt.type.report;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Profile("local")
@Slf4j
public class LocalTypeReportPromptLoader implements TypeReportPromptLoader {

    private static final String PROMPT_PATH = "secret/type-prompt-local.txt";
    private static final String REPAIR_PROMPT_PATH = "secret/type-repair-prompt-local.txt";


    @Override
    public String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_PATH);

            if (!resource.exists()) {
                log.error("유형 리포트 프롬프트 파일이 존재하지 않습니다: {}", PROMPT_PATH);
                throw new BadRequestException(ErrorCode.PROMPT_TYPE_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("로컬 유형 리포트 프롬프트 파일 읽기 실패: {}", PROMPT_PATH, e);
            throw new BadRequestException(ErrorCode.PROMPT_TYPE_FILE_READ_FAILED);
        }
    }

    @Override
    public String loadRepairPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_PATH);

            if (!resource.exists()) {
                log.error("유형 리포트 Repair 프롬프트 파일이 존재하지 않습니다: {}", PROMPT_PATH);
                throw new BadRequestException(ErrorCode.PROMPT_TYPE_REPAIR_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("로컬 유형 리포트 Repair 프롬프트 파일 읽기 실패: {}", PROMPT_PATH, e);
            throw new BadRequestException(ErrorCode.PROMPT_TYPE_REPAIR_FILE_READ_FAILED);
        }
    }
}
