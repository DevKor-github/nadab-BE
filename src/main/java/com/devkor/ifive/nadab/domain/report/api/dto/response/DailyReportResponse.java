package com.devkor.ifive.nadab.domain.report.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 리포트 생성 응답")
public record DailyReportResponse(
        String message,
        String emotion,
        int length
) {
}
