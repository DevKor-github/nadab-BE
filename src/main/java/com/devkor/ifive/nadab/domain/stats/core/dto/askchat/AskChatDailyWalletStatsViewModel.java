package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatDailyWalletStatsViewModel(
        long totalLogCount,
        long pendingLogCount,
        long confirmedLogCount,
        long refundedLogCount,
        long freeTurnsGranted,
        long freeTurnsConsumed,
        long paidTurnsConsumed,
        long freeTurnsRefunded,
        long paidTurnsRefunded,
        long paidTurnsCharged,
        long netFreeTurnDelta,
        long netPaidTurnDelta
) {
}
