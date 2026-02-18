package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 유형 리포트 단일 조회 응답")
public record MyTypeReportResponse(

        @Schema(description = "유형 리포트", nullable = true)
        TypeReportResponse report
) {
}
