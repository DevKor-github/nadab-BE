package com.devkor.ifive.nadab.global.core.prompt.monthly;

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
public class LocalMonthlyReportPromptLoader implements MonthlyReportPromptLoader {

    private static final String V1_PROMPT_PATH = "secret/monthly-prompt-v1-local.txt";
    private static final String V2_BASELINE_PROMPT_PATH = "secret/monthly-prompt-v2-baseline-local.txt";

    @Override
    public String loadV1Prompt() {
        try {
            ClassPathResource resource = new ClassPathResource(V1_PROMPT_PATH);

            if (!resource.exists()) {
                log.error("월간 리포트 프롬프트 파일이 존재하지 않습니다: {}", V1_PROMPT_PATH);
                throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("로컬 월간 리포트 프롬프트 파일 읽기 실패: {}", V1_PROMPT_PATH, e);
            throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_FILE_READ_FAILED);
        }
    }

    @Override
    public String loadV2BaselinePrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(V2_BASELINE_PROMPT_PATH);

            if (!resource.exists()) {
                log.error("월간 리포트 프롬프트 파일이 존재하지 않습니다: {}", V2_BASELINE_PROMPT_PATH);
                throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("로컬 월간 리포트 프롬프트 파일 읽기 실패: {}", V2_BASELINE_PROMPT_PATH, e);
            throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_FILE_READ_FAILED);
        }
    }

    @Override
    public String loadImagePrompt() {
        return """
                Create a premium abstract monthly self-reflection cover image for a Korean journaling app.
               
                Report context:
                - Monthly summary: %s
                - Gentle comment summary: %s
                - Dominant keyword: %s
                - Month: %s to %s
               
                Task:
                Create a symbolic, abstract cover image that represents the emotional meaning of this monthly report. Do not use a fixed scene. Choose objects, colors, light, space, and composition that naturally match the report's theme.
               
                The image should feel calm, warm, reflective, comforting, refined, and suitable for a premium mobile app monthly report card.
               
                Style:
                Premium editorial illustration, modern app-friendly aesthetic, soft lighting, subtle texture, refined color palette, clean composition, enough negative space.
               
                Strict constraints:
                No people, no faces, no hands, no body parts.
                No readable text, no letters, no Korean characters, no English characters, no numbers, no symbols, no handwriting, no typography.
                No UI, no logos, no brand marks, no watermarks.
                No medical, therapy, counseling, hospital, or diagnosis-related imagery.
                No dark, horror, gloomy, childish, or overly dramatic mood.
               """;
    }
}
