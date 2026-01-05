package com.devkor.ifive.nadab.domain.weeklyreport.core.dto;

public record WeeklyReserveResultDto(
        Long reportId,
        Long crystalLogId,
        Long userId,
        Long balanceAfter
) {
}
