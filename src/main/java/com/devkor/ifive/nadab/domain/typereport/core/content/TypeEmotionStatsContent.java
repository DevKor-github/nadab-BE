package com.devkor.ifive.nadab.domain.typereport.core.content;

import java.util.ArrayList;
import java.util.List;

public record TypeEmotionStatsContent(
        Integer totalCount,
        String dominantEmotionCode,
        Integer positivePercent,
        List<EmotionStat> emotions
) {
    public TypeEmotionStatsContent normalized() {
        int normalizedTotalCount = normalizeNonNegative(totalCount);
        int normalizedPositivePercent = normalizePercent(positivePercent);

        List<EmotionStat> normalizedEmotions = normalizeEmotions(emotions);
        String normalizedDominantEmotionCode = normalizeDominantEmotionCode(
                dominantEmotionCode,
                normalizedEmotions
        );

        return new TypeEmotionStatsContent(
                normalizedTotalCount,
                normalizedDominantEmotionCode,
                normalizedPositivePercent,
                List.copyOf(normalizedEmotions)
        );
    }

    private static List<EmotionStat> normalizeEmotions(List<EmotionStat> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<EmotionStat> out = new ArrayList<>();
        for (EmotionStat stat : source) {
            if (stat == null) {
                continue;
            }
            out.add(new EmotionStat(
                    trimToNull(stat.emotionCode()),
                    trimToNull(stat.emotionName()),
                    normalizeNonNegative(stat.count()),
                    normalizePercent(stat.percent())
            ));
        }
        return out;
    }

    private static String normalizeDominantEmotionCode(String dominantEmotionCode, List<EmotionStat> normalizedEmotions) {
        String trimmed = trimToNull(dominantEmotionCode);
        if (trimmed != null) {
            return trimmed;
        }
        if (normalizedEmotions.isEmpty()) {
            return null;
        }
        EmotionStat first = normalizedEmotions.get(0);
        return first == null ? null : trimToNull(first.emotionCode());
    }

    private static int normalizeNonNegative(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private static int normalizePercent(Integer value) {
        int normalized = normalizeNonNegative(value);
        return Math.min(100, normalized);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record EmotionStat(
            String emotionCode,
            String emotionName,
            Integer count,
            Integer percent
    ) {
    }
}
