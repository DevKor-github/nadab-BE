package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업데이트 항목")
public record HomeVersionItemResponse(
        @Schema(description = "업데이트 항목명", example = "월간 리포트")
        String title,

        @Schema(description = "업데이트 상세 설명", example = "한 달의 기록을 한눈에 돌아볼 수 있어요.")
        String description
) {
}
