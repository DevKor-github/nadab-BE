package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record InterestStatsContent(
        List<InterestStat> interests
) {
    public InterestStatsContent normalized() {
        return new InterestStatsContent(List.copyOf(normalizeInterests(interests)));
    }

    private static List<InterestStat> normalizeInterests(List<InterestStat> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<InterestStat> out = new ArrayList<>();
        for (InterestStat stat : source) {
            if (stat == null) {
                continue;
            }
            out.add(new InterestStat(
                    trimToNull(stat.intersetCode()),
                    trimToNull(stat.interestName()),
                    normalizeNonNegative(stat.count())
            ));
        }

        out.sort(Comparator.comparingInt((InterestStat stat) -> stat.count() == null ? 0 : stat.count()).reversed());
        return out;
    }

    private static int normalizeNonNegative(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record InterestStat(
            String intersetCode,
            String interestName,
            Integer count
    ) {
    }
}
