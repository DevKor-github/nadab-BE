package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;

public record AskChatDailyStatsViewModel(
        LocalDate date,
        long sessionCount,
        long uniqueUserCount,
        long activeSessionCount,
        long endedSessionCount,
        long userMessageCount,
        long completedAssistantMessageCount,
        long failedAssistantMessageCount,
        double averageGenerationDurationMs,
        long p95GenerationDurationMs
) {
}
