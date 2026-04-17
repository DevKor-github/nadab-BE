package com.devkor.ifive.nadab.domain.stats.core.dto.type;

import java.util.List;

public record TypeStatsViewModel(
        long inProgressTypeReportCount,
        List<String> interestLabels,
        List<Long> completedTypeReportCounts,
        String refreshedAt
) {}
