package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import java.time.LocalDate;

/**
 * 답변 검색용 Projection DTO
 * Repository 쿼리 결과를 담는 DTO
 */
public record SearchAnswerEntryDto(
        Long answerId,
        InterestCode interestCode,
        EmotionCode emotionCode,
        String questionText,
        String answerContent,
        LocalDate answerDate
) {
}