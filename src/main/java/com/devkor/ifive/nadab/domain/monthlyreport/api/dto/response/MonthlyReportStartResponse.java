package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월간 리포트 생성 시작 응답")
public record MonthlyReportStartResponse(
        @Schema(description = "생성 예정 월간 리포트 ID", example = "1")
        Long reportId,

        @Schema(description = "상태", example = "PENDING")
        String status,

        @Schema(description = "리포트 작성 후 크리스탈 잔액", example = "100")
        Long balanceAfter
) {}
