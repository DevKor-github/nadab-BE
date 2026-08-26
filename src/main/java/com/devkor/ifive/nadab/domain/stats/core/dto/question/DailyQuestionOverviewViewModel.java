package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import java.util.List;

public record DailyQuestionOverviewViewModel(
        List<DailyQuestionOverviewRowViewModel> rows,
        int totalQuestionCount,
        DailyQuestionOverviewQuery query,
        String refreshedAt
) {

    public int filteredQuestionCount() {
        return rows.size();
    }
}
