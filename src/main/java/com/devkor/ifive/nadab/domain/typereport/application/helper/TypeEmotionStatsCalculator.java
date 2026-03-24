package com.devkor.ifive.nadab.domain.typereport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.EmotionStatsCountDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public final class TypeEmotionStatsCalculator {

    private static final EnumSet<EmotionCode> POSITIVE_EMOTIONS = EnumSet.of(
            EmotionCode.ACHIEVEMENT,
            EmotionCode.INTEREST,
            EmotionCode.PEACE,
            EmotionCode.PLEASURE,
            EmotionCode.WILL
    );

    private TypeEmotionStatsCalculator() {
    }

    public static TypeEmotionStatsContent calculate(List<EmotionStatsCountDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return new TypeEmotionStatsContent(0, null, 0, List.of()).normalized();
        }

        List<EmotionStatsCountDto> sorted = rows.stream()
                .filter(r -> r != null && r.emotionCode() != null)
                .sorted(Comparator
                        .comparingLong((EmotionStatsCountDto r) -> safeLong(r.count())).reversed()
                        .thenComparing(r -> r.emotionCode().name()))
                .toList();

        long total = sorted.stream().mapToLong(r -> safeLong(r.count())).sum();
        if (total <= 0) {
            return new TypeEmotionStatsContent(0, null, 0, List.of()).normalized();
        }

        List<TypeEmotionStatsContent.EmotionStat> emotions = sorted.stream()
                .map(r -> new TypeEmotionStatsContent.EmotionStat(
                        r.emotionCode().name(),
                        r.emotionName() == null ? null : r.emotionName().name(),
                        safeInt(r.count()),
                        percent(safeLong(r.count()), total)
                ))
                .toList();

        String topEmotion = sorted.get(0).emotionCode().name();
        long positiveCount = sorted.stream()
                .filter(r -> POSITIVE_EMOTIONS.contains(r.emotionCode()))
                .mapToLong(r -> safeLong(r.count()))
                .sum();

        return new TypeEmotionStatsContent(
                safeInt(total),
                topEmotion,
                percent(positiveCount, total),
                emotions
        ).normalized();
    }

    private static long safeLong(Long value) {
        return value == null ? 0L : Math.max(0L, value);
    }

    private static int safeInt(Long value) {
        long v = safeLong(value);
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
    }

    private static int safeInt(long value) {
        long v = Math.max(0L, value);
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
    }

    private static int percent(long count, long total) {
        if (total <= 0) return 0;
        return (int) Math.round((count * 100.0) / total);
    }
}
