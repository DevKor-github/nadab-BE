package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "버전별 월간 리포트 상세 조회를 위한 위치 정보")
public record MonthlyReportLocatorResponse(
        @Schema(description = "월간 리포트 ID")
        Long reportId,

        @Schema(description = "월간 리포트 버전", allowableValues = {"1", "2"}, example = "2")
        int version,

        @Schema(description = "월간 리포트 대상 월", example = "5")
        int month,

        @Schema(description = "월간 리포트 상태", example = "COMPLETED")
        MonthlyReportStatus status
) {
}
