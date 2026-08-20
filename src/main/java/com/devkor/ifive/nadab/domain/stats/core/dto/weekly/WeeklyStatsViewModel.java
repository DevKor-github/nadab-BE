package com.devkor.ifive.nadab.domain.stats.core.dto.weekly;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;

import java.util.List;

public record WeeklyStatsViewModel(
        List<String> labels,
        List<Long> signupCounts,
        List<Long> assignedQuestionCounts,
        List<Long> completedDailyReportCounts,
        List<Long> completedWeeklyReportCounts,
        List<Long> wauCounts,
        WeeklyPeriodStatsViewModel selectedPeriod,
        PeakStatViewModel signupPeak,
        PeakStatViewModel assignedQuestionPeak,
        PeakStatViewModel completedDailyReportPeak,
        PeakStatViewModel completedWeeklyReportPeak,
        PeakStatViewModel wauPeak,
        long inProgressWeeklyReportCount,
        String refreshedAt
) {}
