package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;

public record AskChatDailySessionStatsDto(
        LocalDate date,
        long sessionCount,
        long uniqueUserCount,
        long activeSessionCount,
        long endedSessionCount
) {
}
