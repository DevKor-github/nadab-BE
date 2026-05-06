package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageStatusResponse(
        @Schema(description = "이미지 상태", example = "READY, PROCESSING")
        String status
) {
}
