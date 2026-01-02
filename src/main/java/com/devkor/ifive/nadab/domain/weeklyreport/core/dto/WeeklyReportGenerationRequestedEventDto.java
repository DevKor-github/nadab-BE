package com.devkor.ifive.nadab.domain.weeklyreport.core.dto;

public record WeeklyReportGenerationRequestedEventDto(
        Long reportId,
        Long userId,
        Long crystalLogId
) {}

