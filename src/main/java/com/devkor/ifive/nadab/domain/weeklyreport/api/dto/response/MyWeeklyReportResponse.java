package com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 주간 리포트 조회 응답")
public record MyWeeklyReportResponse(

        @Schema(description = "이번 주간 리포트", nullable = true)
        WeeklyReportResponse report,

        @Schema(description = "이전 주간 리포트", nullable = true)
        WeeklyReportResponse previousReport
) {
}
