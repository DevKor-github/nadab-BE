package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;

public record MonthlyEmotionComparisonContent(
        Long previousReportId,
        Integer previousMonth,
        TypeEmotionStatsContent previousEmotionStats,
        Integer positivePercentPointChange
) {
    public static MonthlyEmotionComparisonContent from(MonthlyReportComparisonInputDto input) {
        if (input == null) {
            return null;
        }

        Integer previousMonth = input.previousMonthStartDate() == null
                ? null
                : input.previousMonthStartDate().getMonthValue();

        return new MonthlyEmotionComparisonContent(
                input.previousReportId(),
                previousMonth,
                input.previousEmotionStats(),
                input.positivePercentPointChange()
        ).normalized();
    }

    public MonthlyEmotionComparisonContent normalized() {
        TypeEmotionStatsContent normalizedPreviousEmotionStats = previousEmotionStats == null
                ? TypeContentFactory.emptyEmotionStats()
                : previousEmotionStats.normalized();
        int normalizedPositivePercentPointChange = positivePercentPointChange == null
                ? 0
                : Math.max(-100, Math.min(100, positivePercentPointChange));

        return new MonthlyEmotionComparisonContent(
                previousReportId,
                previousMonth,
                normalizedPreviousEmotionStats,
                normalizedPositivePercentPointChange
        );
    }
}
