package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

public record MonthlyReportGenerationRequestedEventDtoV2(
        Long reportId,
        Long userId,
        Long crystalLogId,
        boolean exists
) {
}
