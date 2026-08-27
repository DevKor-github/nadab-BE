package com.devkor.ifive.nadab.domain.stats.core.dto.question;

public record DailyQuestionReactionStatsViewModel(
        long exposureCount,
        long answeredCount,
        long rerolledCount,
        long unansweredCount
) {

    public double answerRate() {
        return exposureCount == 0L ? 0.0 : (double) answeredCount / exposureCount;
    }

    public double answerRatePercent() {
        return answerRate() * 100.0;
    }
}
