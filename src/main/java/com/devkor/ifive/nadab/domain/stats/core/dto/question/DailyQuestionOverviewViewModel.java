package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

public record DailyQuestionOverviewViewModel(
        List<DailyQuestionOverviewRowViewModel> rows,
        int totalQuestionCount,
        DailyQuestionOverviewQuery query,
        @Nullable OffsetDateTime baselineEffectiveFrom,
        String refreshedAt
) {

    public int filteredQuestionCount() {
        return rows.size();
    }
}
