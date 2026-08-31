package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatDailyRagStatsViewModel(
        long totalDocumentCount,
        long pendingDocumentCount,
        long completedDocumentCount,
        long failedDocumentCount,
        long deadLetterDocumentCount,
        double averageFailedRetryCount,
        long referenceCount,
        long uniqueReferencedDocumentCount
) {
}
