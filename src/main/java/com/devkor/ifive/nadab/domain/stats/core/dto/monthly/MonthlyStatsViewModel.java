package com.devkor.ifive.nadab.domain.stats.core.dto.monthly;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;

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
        PeakStatViewModel signupPeak,
        PeakStatViewModel assignedQuestionPeak,
        PeakStatViewModel completedDailyReportPeak,
        PeakStatViewModel completedMonthlyReportPeak,
        PeakStatViewModel mauPeak,
        long inProgressMonthlyReportV1Count,
        long inProgressMonthlyReportV2Count,
        long inProgressMonthlyReportTotalCount,
        String refreshedAt
) {}
