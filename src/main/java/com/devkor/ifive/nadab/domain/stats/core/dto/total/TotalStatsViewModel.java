package com.devkor.ifive.nadab.domain.stats.core.dto.total;

import java.util.List;

public record TotalStatsViewModel(

        long totalUserCount,

        // provider labels: ["GOOGLE","KAKAO","NAVER","NORMAL"]
        List<String> providerLabels,
        List<Long> providerCounts,

        // interest labels: ["PREFERENCE", ...]
        List<String> interestLabels,
        List<Long> interestSelectedCounts,   // pie
        List<Long> interestDailyReportCounts, // bar (COMPLETED)

        String refreshedAt
) {}
