package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;

public record AskChatDailyMessageStatsDto(
        LocalDate date,
        long userMessageCount,
        long completedAssistantMessageCount,
        long failedAssistantMessageCount,
        double averageGenerationDurationMs,
        long p95GenerationDurationMs
) {
}
