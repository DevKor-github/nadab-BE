package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

public record MonthlyReserveResultDto(
        Long reportId,
        Long crystalLogId,
        Long userId,
        Long balanceAfter
) {
}
