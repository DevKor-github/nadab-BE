package com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주간 리포트 생성 시작 응답")
public record WeeklyReportStartResponse(
        @Schema(description = "생성 예정 주간 리포트 ID", example = "1")
        Long reportId,

        @Schema(description = "상태", example = "PENDING")
        String status
) {}
