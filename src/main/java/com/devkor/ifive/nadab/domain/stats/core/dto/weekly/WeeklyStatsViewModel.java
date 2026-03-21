package com.devkor.ifive.nadab.domain.stats.core.dto.weekly;

import java.util.List;

public record WeeklyStatsViewModel(
        List<String> labels,
        List<Long> signupCounts,
        List<Long> assignedQuestionCounts,
        List<Long> completedWeeklyReportCounts,
        long inProgressWeeklyReportCount,
        String refreshedAt
) {}
