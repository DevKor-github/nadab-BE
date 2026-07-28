package com.devkor.ifive.nadab.domain.askchat.core.dto;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

public record AskChatRagBackfillTargetDto(
        Long answerEntryId,
        Long reportId,
        InterestCode interestCode
) {
}
