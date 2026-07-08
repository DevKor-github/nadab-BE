package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import java.util.List;

public record MonthlySocialSummaryContent(
        boolean visible,
        int month,
        List<MonthlySocialRankingItem> likeRanking,
        List<MonthlySocialRankingItem> commentRanking
) {
    private static final int MAX_RANKING_SIZE = 3;

    public static MonthlySocialSummaryContent empty(int month) {
        return new MonthlySocialSummaryContent(
                false,
                month,
                List.of(),
                List.of()
        );
    }

    public MonthlySocialSummaryContent normalized() {
        return new MonthlySocialSummaryContent(
                visible,
                Math.max(1, Math.min(12, month)),
                normalizeRanking(likeRanking),
                normalizeRanking(commentRanking)
        );
    }

    private static List<MonthlySocialRankingItem> normalizeRanking(List<MonthlySocialRankingItem> ranking) {
        if (ranking == null || ranking.isEmpty()) {
            return List.of();
        }

        return ranking.stream()
                .filter(item -> item != null)
                .limit(MAX_RANKING_SIZE)
                .map(MonthlySocialRankingItem::normalized)
                .toList();
    }
}
