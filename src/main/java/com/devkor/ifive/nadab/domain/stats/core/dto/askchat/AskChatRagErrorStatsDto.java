package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatRagErrorStatsDto(
        String errorCode,
        long count
) {
}
