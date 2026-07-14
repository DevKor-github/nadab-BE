package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillResultDto;

public record AdminAskChatRagBackfillResponse(
        int targetCount,
        int indexedCount,
        int failedCount
) {

    public static AdminAskChatRagBackfillResponse from(AskChatRagBackfillResultDto result) {
        return new AdminAskChatRagBackfillResponse(
                result.targetCount(),
                result.indexedCount(),
                result.failedCount()
        );
    }
}
