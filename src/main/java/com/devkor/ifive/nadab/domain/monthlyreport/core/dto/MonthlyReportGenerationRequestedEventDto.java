package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

public record MonthlyReportGenerationRequestedEventDto(
        Long reportId,
        Long userId,
        Long crystalLogId
) {
}
