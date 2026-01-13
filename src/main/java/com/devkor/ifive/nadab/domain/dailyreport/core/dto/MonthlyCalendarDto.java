package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;

import java.time.LocalDate;

/**
 * 월별 캘린더 쿼리 결과 DTO
 */
public record MonthlyCalendarDto(
        LocalDate date,
        EmotionCode emotionCode // null 가능 (리포트가 없거나 PENDING/FAILED 상태)
) {
}
