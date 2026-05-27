package com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal;

import java.util.List;

public record WithdrawalStatsViewModel(
        int recentEventLimit,
        long eventCount,
        List<String> reasonLabels,
        List<Long> reasonCounts,
        List<WithdrawalEventRowViewModel> rows,
        String refreshedAt
) {
}
