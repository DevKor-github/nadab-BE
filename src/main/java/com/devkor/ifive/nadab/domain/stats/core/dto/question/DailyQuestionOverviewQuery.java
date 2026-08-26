package com.devkor.ifive.nadab.domain.stats.core.dto.question;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import javax.annotation.Nullable;
import java.util.Objects;

public record DailyQuestionOverviewQuery(
        String keyword,
        @Nullable InterestCode interestCode,
        @Nullable Integer questionLevel,
        @Nullable Boolean active,
        long minimumCurrentExposureCount,
        DailyQuestionOverviewSort sort,
        DailyQuestionOverviewSortDirection direction
) {

    public DailyQuestionOverviewQuery {
        keyword = keyword == null ? "" : keyword.trim();
        if (questionLevel != null && questionLevel < 1) {
            throw new IllegalArgumentException("questionLevel must be positive");
        }
        if (minimumCurrentExposureCount < 0L) {
            throw new IllegalArgumentException("minimumCurrentExposureCount must not be negative");
        }
        Objects.requireNonNull(sort, "sort must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
    }

    public static DailyQuestionOverviewQuery defaults() {
        return new DailyQuestionOverviewQuery(
                "",
                null,
                null,
                null,
                0L,
                DailyQuestionOverviewSort.CURRENT_EXPOSURE_COUNT,
                DailyQuestionOverviewSortDirection.DESC
        );
    }
}
