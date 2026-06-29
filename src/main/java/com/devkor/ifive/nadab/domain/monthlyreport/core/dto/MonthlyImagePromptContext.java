package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;

import java.time.LocalDate;
import java.util.Objects;

public record MonthlyImagePromptContext(
        String summary,
        String commentSummary,
        String dominantKeyword,
        LocalDate monthStartDate,
        LocalDate monthEndDate,
        MonthlyImageStylePreset stylePreset
) {

    public MonthlyImagePromptContext {
        summary = normalize(summary);
        commentSummary = normalize(commentSummary);
        dominantKeyword = normalize(dominantKeyword);
        Objects.requireNonNull(monthStartDate, "monthStartDate must not be null");
        Objects.requireNonNull(monthEndDate, "monthEndDate must not be null");
        Objects.requireNonNull(stylePreset, "stylePreset must not be null");
    }

    public static MonthlyImagePromptContext from(
            AiMonthlyReportResultDto result,
            MonthRangeDto range,
            MonthlyImageStylePreset stylePreset
    ) {
        Objects.requireNonNull(result, "result must not be null");
        MonthlyReportV2Content content = Objects.requireNonNull(result.content(), "content must not be null");
        Objects.requireNonNull(range, "range must not be null");

        return new MonthlyImagePromptContext(
                content.summary(),
                content.commentSummary(),
                content.dominantKeyword(),
                range.monthStartDate(),
                range.monthEndDate(),
                stylePreset
        );
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "Not provided" : value.trim();
    }
}
