package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import java.time.OffsetDateTime;

public record DailyQuestionOverviewRowViewModel(
        long questionId,
        InterestCode interestCode,
        String questionText,
        int questionLevel,
        int currentRevisionNo,
        OffsetDateTime deletedAt,
        OffsetDateTime currentRevisionEffectiveFrom,
        long currentExposureCount,
        long currentAnsweredCount,
        long currentRerolledCount,
        long currentUnansweredCount,
        long totalExposureCount,
        long totalAnsweredCount,
        long totalRerolledCount,
        long totalUnansweredCount
) {

    public boolean active() {
        return deletedAt == null;
    }

    public double currentAnswerRate() {
        return rate(currentAnsweredCount, currentExposureCount);
    }

    public double currentAnswerRatePercent() {
        return currentAnswerRate() * 100.0;
    }

    public double currentRerollRate() {
        return rate(currentRerolledCount, currentExposureCount);
    }

    public double currentRerollRatePercent() {
        return currentRerollRate() * 100.0;
    }

    public double totalAnswerRate() {
        return rate(totalAnsweredCount, totalExposureCount);
    }

    public double totalAnswerRatePercent() {
        return totalAnswerRate() * 100.0;
    }

    private double rate(long count, long exposureCount) {
        return exposureCount == 0L ? 0.0 : (double) count / exposureCount;
    }
}
