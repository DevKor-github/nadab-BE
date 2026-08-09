package com.devkor.ifive.nadab.domain.stats.core.dto.monthly;

import java.util.List;

public record MonthlyStatsViewModel(
        List<String> labels,
        List<Long> signupCounts,
        List<Long> assignedQuestionCounts,
        List<Long> completedDailyReportCounts,
        List<Long> completedMonthlyReportV1Counts,
        List<Long> completedMonthlyReportV2Counts,
        List<Long> completedMonthlyReportTotalCounts,
        List<Long> mauCounts,
        long inProgressMonthlyReportV1Count,
        long inProgressMonthlyReportV2Count,
        long inProgressMonthlyReportTotalCount,
        String refreshedAt
) {}
