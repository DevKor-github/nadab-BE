package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

/**
 * 피드 조회용 DTO
 */
public record FeedDto(
        String nickname,
        String profileImageKey,
        DefaultProfileType defaultProfileType,
        InterestCode interestCode,
        String questionText,
        String answerContent,
        EmotionCode emotionCode
) {
}
