package com.devkor.ifive.nadab.domain.askchat.core.dto;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

public record AskChatAnswerReferenceDocument(
        Long documentId,
        AskChatRagDocumentSourceType sourceType,
        Long sourceId,
        InterestCode interestCode,
        String content,
        double distance
) {

    public static AskChatAnswerReferenceDocument from(AskChatRagSearchResultDto result) {
        return new AskChatAnswerReferenceDocument(
                result.documentId(),
                result.sourceType(),
                result.sourceId(),
                result.interestCode(),
                result.content(),
                result.distance()
        );
    }
}
