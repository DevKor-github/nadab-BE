package com.devkor.ifive.nadab.domain.stats.core.dto.daily;

import java.util.List;

public record DailyStatsViewModel(
        List<String> labels,              // yyyy-MM-dd
        List<Long> signupCounts,
        List<Long> assignedQuestionCounts,
        List<Long> completedDailyReportCounts,
        long sharedDailyReportCount,
        String refreshedAt // "2026-02-27 21:34:12"
) {}
