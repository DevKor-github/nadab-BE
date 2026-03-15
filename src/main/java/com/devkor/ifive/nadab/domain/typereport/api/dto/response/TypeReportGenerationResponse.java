package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유형 리포트 생성 상태")
public record TypeReportGenerationResponse(

        @Schema(description = "생성 상태", example = "NONE")
        TypeReportGenerationStatus status,

        @Schema(description = "생성 중/실패한 작업의 reportId (없으면 null)", nullable = true)
        Long reportId
) {
}