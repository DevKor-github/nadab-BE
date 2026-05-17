package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.InterestStatsContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.InterestStatsCountDto;

import java.util.Comparator;
import java.util.List;

public final class MonthlyInterestStatsCalculator {

    private MonthlyInterestStatsCalculator() {
    }

    public static InterestStatsContent calculate(List<InterestStatsCountDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return new InterestStatsContent(List.of()).normalized();
        }

        List<InterestStatsContent.InterestStat> interests = rows.stream()
                .filter(r -> r != null && r.interestCode() != null)
                .sorted(Comparator
                        .comparingLong((InterestStatsCountDto r) -> safeLong(r.count())).reversed()
                        .thenComparing(r -> r.interestCode().name()))
                .map(r -> new InterestStatsContent.InterestStat(
                        r.interestCode().name(),
                        r.interestName(),
                        safeInt(r.count())
                ))
                .toList();

        return new InterestStatsContent(interests).normalized();
    }

    private static long safeLong(Long value) {
        return value == null ? 0L : Math.max(0L, value);
    }

    private static int safeInt(Long value) {
        long v = safeLong(value);
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
    }
}
