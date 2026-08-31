package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

public record AskChatErrorStatsDto(
        String errorCode,
        long count
) {
}
