package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.user.core.entity.User;

import java.time.LocalDate;

/**
 * 사용자와 마지막 답변일
 * - 미답변 알림 스케줄러용
 */
public record UserWithLastAnswerDate(
    User user,
    LocalDate lastAnswerDate
) {
}