package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatSessionSummaryDto(
        long totalSessionCount,
        long totalUniqueUserCount,
        long totalActiveSessionCount,
        long totalEndedSessionCount
) {
}
