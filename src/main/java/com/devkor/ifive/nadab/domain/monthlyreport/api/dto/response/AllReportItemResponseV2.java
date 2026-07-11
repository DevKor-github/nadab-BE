package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "전체 리포트 목록 아이템")
public record AllReportItemResponseV2(
        @Schema(description = "리포트 ID")
        Long id,
        @Schema(description = "리포트 타입", example = "MONTHLY")
        String type,
        @Schema(description = "기간 문자열", example = "1월 4주차")
        String period,
        @Schema(description = "요약")
        String summary,
        @Schema(description = "리포트 버전", example = "2")
        int version
) {
}
