package com.devkor.ifive.nadab.domain.stats.core.dto.monthly;

import java.util.List;

public record MonthlyStatsViewModel(
        List<String> labels,
        List<Long> signupCounts,
        List<Long> assignedQuestionCounts,
        List<Long> completedMonthlyReportCounts,
        long inProgressMonthlyReportCount,
        String refreshedAt
) {}
