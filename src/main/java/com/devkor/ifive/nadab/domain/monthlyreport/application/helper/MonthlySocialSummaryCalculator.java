package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialRankingItem;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MonthlySocialSummaryCalculator {

    private static final long MIN_INTERACTION_COUNT = 10;
    private static final int MAX_RANKING_SIZE = 3;

    private MonthlySocialSummaryCalculator() {
    }

    public static MonthlySocialSummaryContent calculate(
            int month,
            List<MonthlySocialInteractionCountDto> likeCounts,
            List<MonthlySocialInteractionCountDto> commentCounts
    ) {
        List<MonthlySocialInteractionCountDto> normalizedLikes = normalize(likeCounts);
        List<MonthlySocialInteractionCountDto> normalizedComments = normalize(commentCounts);

        boolean visible = !normalizedLikes.isEmpty()
                && !normalizedComments.isEmpty()
                && totalCount(normalizedLikes) + totalCount(normalizedComments) >= MIN_INTERACTION_COUNT;
        if (!visible) {
            return MonthlySocialSummaryContent.empty(month);
        }

        return new MonthlySocialSummaryContent(
                true,
                month,
                toRanking(normalizedLikes),
                toRanking(normalizedComments)
        ).normalized();
    }

    private static List<MonthlySocialInteractionCountDto> normalize(
            List<MonthlySocialInteractionCountDto> counts
    ) {
        if (counts == null || counts.isEmpty()) {
            return List.of();
        }

        return counts.stream()
                .filter(item -> item != null && item.userId() != null && item.interactionCount() > 0)
                .toList();
    }

    private static long totalCount(List<MonthlySocialInteractionCountDto> counts) {
        return counts.stream()
                .mapToLong(MonthlySocialInteractionCountDto::interactionCount)
                .sum();
    }

    private static List<MonthlySocialRankingItem> toRanking(
            List<MonthlySocialInteractionCountDto> counts
    ) {
        Collator koreanCollator = Collator.getInstance(Locale.KOREAN);
        Comparator<MonthlySocialInteractionCountDto> comparator = Comparator
                .comparingLong(MonthlySocialInteractionCountDto::interactionCount)
                .reversed()
                .thenComparing(
                        item -> normalizeNickname(item.nickname()),
                        koreanCollator
                )
                .thenComparing(item -> normalizeNickname(item.nickname()))
                .thenComparing(MonthlySocialInteractionCountDto::userId);

        List<MonthlySocialInteractionCountDto> sorted = counts.stream()
                .sorted(comparator)
                .limit(MAX_RANKING_SIZE)
                .toList();
        long topCount = sorted.getFirst().interactionCount();

        return java.util.stream.IntStream.range(0, sorted.size())
                .mapToObj(index -> toRankingItem(sorted.get(index), index + 1, topCount))
                .toList();
    }

    private static MonthlySocialRankingItem toRankingItem(
            MonthlySocialInteractionCountDto item,
            int displayOrder,
            long topCount
    ) {
        return new MonthlySocialRankingItem(
                displayOrder,
                item.userId(),
                normalizeNickname(item.nickname()),
                item.profileImageKey(),
                item.defaultProfileType(),
                item.interactionCount() == topCount
        );
    }

    private static String normalizeNickname(String nickname) {
        return nickname == null ? "" : nickname.trim();
    }
}
