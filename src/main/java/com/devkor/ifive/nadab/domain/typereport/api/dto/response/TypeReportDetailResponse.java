package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개별 유형 리포트 상세 상태 및 생성 조건")
public record TypeReportDetailResponse(

        @Schema(description = "현재 조회 가능한 유형 리포트 (없거나 생성 전이면 null)")
        TypeReportResponse current,

        @Schema(description = "리포트 생성 상태")
        TypeReportGenerationResponse generation,

        @Schema(description = "리포트 생성 자격 조건 및 현황")
        TypeReportEligibilityResponse eligibility

) {
}
