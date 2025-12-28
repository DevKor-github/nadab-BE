package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

public record TestDailyReportResponse(
    String message,
    String emotion,
    int length
) {
}
