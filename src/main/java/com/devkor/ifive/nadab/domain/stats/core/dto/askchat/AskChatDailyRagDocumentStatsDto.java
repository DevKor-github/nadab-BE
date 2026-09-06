package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;

public record AskChatDailyRagDocumentStatsDto(
        LocalDate date,
        long totalDocumentCount,
        long pendingDocumentCount,
        long completedDocumentCount,
        long failedDocumentCount,
        long deadLetterDocumentCount,
        double averageFailedRetryCount
) {
}
