package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 현재·이전 월간 리포트 위치 조회 응답")
public record MyMonthlyReportLookupResponseV2(
        @Schema(description = "지난달 월간 리포트 위치 정보. 리포트가 없으면 null", nullable = true)
        MonthlyReportLocatorResponse report,

        @Schema(description = "지지난달 완료 월간 리포트 위치 정보. 리포트가 없으면 null", nullable = true)
        MonthlyReportLocatorResponse previousReport
) {
}
