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

    @Value("${MONTHLY_PROMPT}")
    private String rawPrompt;

    @Override
    public String loadPrompt() {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            log.error("환경 변수 MONTHLY_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_MONTHLY_ENV_VAR_NOT_SET);
        }

        return rawPrompt;
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
