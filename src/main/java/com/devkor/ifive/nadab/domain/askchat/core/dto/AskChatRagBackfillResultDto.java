package com.devkor.ifive.nadab.domain.askchat.core.dto;

public record AskChatRagBackfillResultDto(
        int targetCount,
        int indexedCount,
        int failedCount
) {
}
