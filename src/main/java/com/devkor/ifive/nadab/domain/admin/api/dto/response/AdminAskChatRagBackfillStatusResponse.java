package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillStatusDto;

public record AdminAskChatRagBackfillStatusResponse(
        long targetCount,
        long indexedCount,
        long failedCount
) {

    public static AdminAskChatRagBackfillStatusResponse from(AskChatRagBackfillStatusDto status) {
        return new AdminAskChatRagBackfillStatusResponse(
                status.targetCount(),
                status.indexedCount(),
                status.failedCount()
        );
    }
}
