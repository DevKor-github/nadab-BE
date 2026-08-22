package com.devkor.ifive.nadab.domain.stats.core.dto.monthly;

public record MonthlyPeriodStatsViewModel(
        String periodValue,
        String periodLabel,
        long signupCount,
        long assignedQuestionCount,
        long completedDailyReportCount,
        long completedMonthlyReportV1Count,
        long completedMonthlyReportV2Count,
        long completedMonthlyReportTotalCount,
        long mauCount
) {}
