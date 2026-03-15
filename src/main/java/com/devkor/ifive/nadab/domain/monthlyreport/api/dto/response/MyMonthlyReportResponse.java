package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 월간 리포트 조회 응답")
public record MyMonthlyReportResponse(

        @Schema(description = "이번 월간 리포트", nullable = true)
        MonthlyReportResponse report,

        @Schema(description = "이전 월간 리포트", nullable = true)
        MonthlyReportResponse previousReport
) {
}