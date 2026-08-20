package com.devkor.ifive.nadab.domain.stats.core.dto.daily;

public record DailyPeriodStatsViewModel(
        String periodValue,
        String periodLabel,
        long signupCount,
        long assignedQuestionCount,
        long dauCount,
        long sharedDailyReportCount
) {}
