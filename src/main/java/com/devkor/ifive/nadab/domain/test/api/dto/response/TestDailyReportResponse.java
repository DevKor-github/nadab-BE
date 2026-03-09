package com.devkor.ifive.nadab.domain.test.api.dto.response;

public record TestDailyReportResponse(
    String message,
    String emotion,
    int length
) {
}
