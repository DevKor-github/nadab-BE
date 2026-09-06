package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatMessageSummaryDto(
        long totalUserMessageCount,
        long totalCompletedAssistantMessageCount,
        long totalFailedAssistantMessageCount,
        double averageGenerationDurationMs,
        long p95GenerationDurationMs
) {
}
