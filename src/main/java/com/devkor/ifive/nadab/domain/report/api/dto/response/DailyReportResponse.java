package com.devkor.ifive.nadab.domain.report.api.dto.response;

public record DailyReportResponse(
        String message,
        String emotion,
        int length
) {
}
