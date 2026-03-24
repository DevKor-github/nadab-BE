package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;

public record EmotionStatsCountDto(
        EmotionCode emotionCode,
        EmotionName emotionName,
        Long count
) {
}
