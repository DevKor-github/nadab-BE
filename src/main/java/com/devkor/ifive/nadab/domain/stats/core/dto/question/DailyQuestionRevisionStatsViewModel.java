package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import java.time.OffsetDateTime;

public record DailyQuestionRevisionStatsViewModel(
        long revisionId,
        int revisionNo,
        InterestCode interestCode,
        String questionText,
        int questionLevel,
        String empathyGuide,
        String hintGuide,
        String leadingQuestionGuide,
        OffsetDateTime deletedAt,
        OffsetDateTime effectiveFrom,
        String sourceMigration,
        long exposureCount,
        long answeredCount,
        long rerolledCount,
        long unansweredCount
) {

    public boolean active() {
        return deletedAt == null;
    }

    public double answerRate() {
        return exposureCount == 0L ? 0.0 : (double) answeredCount / exposureCount;
    }

    public double answerRatePercent() {
        return answerRate() * 100.0;
    }
}
