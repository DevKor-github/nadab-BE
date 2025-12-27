package com.devkor.ifive.nadab.global.core.prompt;

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
public class LocalDailyReportPromptLoader implements DailyReportPromptLoader {

    private static final String PROMPT_PATH = "secret/backup/daily-prompt-local.txt";

    @Override
    public String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_PATH);

            if (!resource.exists()) {
                throw new BadRequestException("프롬프트 파일이 존재하지 않습니다: " + PROMPT_PATH);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("로컬 프롬프트 파일 읽기 실패: {}", PROMPT_PATH, e);
            throw new BadRequestException("로컬 프롬프트 파일을 읽을 수 없습니다: " + PROMPT_PATH);
        }
    }
}
