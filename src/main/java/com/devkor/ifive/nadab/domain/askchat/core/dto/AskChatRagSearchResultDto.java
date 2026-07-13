package com.devkor.ifive.nadab.domain.askchat.core.dto;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

public record AskChatRagSearchResultDto(
        Long documentId,
        AskChatRagDocumentSourceType sourceType,
        Long sourceId,
        InterestCode interestCode,
        String content,
        double distance
) {
}
