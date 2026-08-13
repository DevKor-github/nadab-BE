package com.devkor.ifive.nadab.domain.askchat.core.dto;

public record AskChatRagBackfillStatusDto(
        long targetCount,
        long indexedCount,
        long failedCount
) {
}
