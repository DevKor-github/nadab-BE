package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 월간 리포트 라우팅 정보")
public record MyMonthlyReportLookupItemV2(
        @Schema(description = "리포트 ID")
        Long id,
        @Schema(description = "리포트 버전", example = "2")
        int version,
        @Schema(description = "리포트 대상 월")
        int month,
        @Schema(description = "리포트 상태", example = "COMPLETED")
        MonthlyReportStatus status
) {
}
