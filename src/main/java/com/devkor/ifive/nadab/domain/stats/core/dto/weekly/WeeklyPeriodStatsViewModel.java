package com.devkor.ifive.nadab.domain.stats.core.dto.weekly;

public record WeeklyPeriodStatsViewModel(
        String periodValue,
        String periodLabel,
        long signupCount,
        long assignedQuestionCount,
        long completedDailyReportCount,
        long completedWeeklyReportCount,
        long wauCount
) {}
