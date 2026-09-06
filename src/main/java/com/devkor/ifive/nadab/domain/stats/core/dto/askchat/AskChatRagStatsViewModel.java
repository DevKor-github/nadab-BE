package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.util.List;

public record AskChatRagStatsViewModel(
        long totalDocumentCount,
        long pendingDocumentCount,
        long completedDocumentCount,
        long failedDocumentCount,
        long deadLetterDocumentCount,
        double embeddingCompletionRatePercent,
        double averageFailedRetryCount,
        long totalReferenceCount,
        long uniqueReferencedDocumentCount,
        List<AskChatRagSourceStatsDto> sourceStats,
        List<AskChatRagErrorStatsDto> errorStats
) {
}
