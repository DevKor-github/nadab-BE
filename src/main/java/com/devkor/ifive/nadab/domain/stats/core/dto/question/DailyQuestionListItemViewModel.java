package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import java.time.OffsetDateTime;

public record DailyQuestionListItemViewModel(
        long questionId,
        InterestCode interestCode,
        String questionText,
        int questionLevel,
        int currentRevisionNo,
        OffsetDateTime deletedAt
) {

    public boolean active() {
        return deletedAt == null;
    }
}
