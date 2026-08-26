package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import javax.annotation.Nullable;
import java.util.List;

public record DailyQuestionStatsViewModel(
        List<DailyQuestionListItemViewModel> questions,
        @Nullable DailyQuestionListItemViewModel selectedQuestion,
        DailyQuestionReactionStatsViewModel total,
        List<DailyQuestionRevisionStatsViewModel> revisions,
        String refreshedAt
) {
}
