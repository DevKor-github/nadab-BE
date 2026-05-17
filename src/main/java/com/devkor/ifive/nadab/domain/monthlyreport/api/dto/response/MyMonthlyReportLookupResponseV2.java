package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 월간 리포트 단건 조회 응답")
public record MyMonthlyReportLookupResponseV2(
        @Schema(description = "월간 리포트 라우팅 정보. 리포트가 없으면 null", nullable = true)
        MyMonthlyReportLookupItemV2 report
) {
}
