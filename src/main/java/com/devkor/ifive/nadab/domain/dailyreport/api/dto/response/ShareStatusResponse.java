package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피드 공유 상태 응답")
public record ShareStatusResponse(

        @Schema(description = "오늘의 기록 공유 상태", example = "true")
        Boolean isShared
) {
}
