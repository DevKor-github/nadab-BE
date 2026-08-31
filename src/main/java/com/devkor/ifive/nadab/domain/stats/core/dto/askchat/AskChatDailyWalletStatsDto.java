package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;

public record AskChatDailyWalletStatsDto(
        LocalDate date,
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
