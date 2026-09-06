package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatRagSummaryDto(
        long totalDocumentCount,
        long pendingDocumentCount,
        long completedDocumentCount,
        long failedDocumentCount,
        long deadLetterDocumentCount,
        double averageFailedRetryCount,
        long totalReferenceCount,
        long uniqueReferencedDocumentCount
) {
}
