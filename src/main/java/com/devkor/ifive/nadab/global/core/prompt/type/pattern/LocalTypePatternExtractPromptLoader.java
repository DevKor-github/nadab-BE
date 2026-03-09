package com.devkor.ifive.nadab.global.core.prompt.type.pattern;

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
public class LocalTypePatternExtractPromptLoader implements TypePatternExtractPromptLoader {

    private static final String PROMPT_PATH = "secret/pattern-extract-prompt-local.txt";

    @Override
    public String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_PATH);

            if (!resource.exists()) {
                log.error("Pattern Extract 프롬프트 파일이 존재하지 않습니다: {}", PROMPT_PATH);
                throw new BadRequestException(ErrorCode.PROMPT_EVIDENCE_CARD_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("로컬 Pattern Extract 프롬프트 파일 읽기 실패: {}", PROMPT_PATH, e);
            throw new BadRequestException(ErrorCode.PROMPT_EVIDENCE_CARD_FILE_READ_FAILED);
        }
    }
}
