package com.devkor.ifive.nadab.domain.typereport.core.content;

import java.util.List;

public record TypeEmotionStatsContent(
        Integer totalCount,
        String dominantEmotionCode,
        Integer positivePercent,
        List<EmotionStat> emotions
) {
    public record EmotionStat(
            String emotionCode,
            String emotionName,
            Integer count,
            Integer percent
    ) {
    }
}
