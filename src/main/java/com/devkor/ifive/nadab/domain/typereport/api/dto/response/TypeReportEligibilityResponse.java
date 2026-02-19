package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유형 리포트 생성 자격 요건 및 현황")
public record TypeReportEligibilityResponse(

        @Schema(description = "일간 리포트 총 개수", example = "3")
        int dailyCompletedCount,

        @Schema(description = "리포트 생성을 위해 필요한 총 질문 개수", example = "30")
        int requiredCount,

        @Schema(description = "리포트 생성 가능 여부 (조건 충족 여부)", example = "false")
        boolean canGenerate,

        @Schema(description = "해당 관심 주제의 유형 리포트 첫 생성 무료 여부", example = "true")
        boolean isFirstFree
) {
}
