package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지난달 월간 리포트 단건 위치 조회 응답")
public record CurrentMonthlyReportLookupResponseV2(
        @Schema(description = "지난달 월간 리포트 위치 정보. 리포트가 없으면 null", nullable = true)
        MonthlyReportLocatorResponse report
) {
}
