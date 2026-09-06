package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatRagSourceStatsDto(
        String sourceType,
        long totalDocumentCount,
        long pendingDocumentCount,
        long completedDocumentCount,
        long failedDocumentCount,
        long deadLetterDocumentCount
) {
}
