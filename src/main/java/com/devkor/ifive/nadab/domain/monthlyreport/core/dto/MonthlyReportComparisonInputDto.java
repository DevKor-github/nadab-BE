package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;

import java.time.LocalDate;
import java.util.List;

public record MonthlyReportComparisonInputDto(
        Long previousReportId,
        LocalDate previousMonthStartDate,
        LocalDate previousMonthEndDate,
        MonthlyReportV2Content previousContent,
        TypeTextContent previousEmotionSummaryContent,
        TypeEmotionStatsContent previousEmotionStats,
        int currentPositivePercent,
        int previousPositivePercent,
        int positivePercentPointChange,
        List<EmotionChange> emotionChanges
) {
    public record EmotionChange(
            String emotionCode,
            String emotionName,
            int currentPercent,
            int previousPercent,
            int changePercentPoint
    ) {
    }
}
