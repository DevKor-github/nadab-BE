package com.devkor.ifive.nadab.domain.dailyreport.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "홈 버전 업데이트 다시 보지 않기 요청")
public record HomeVersionDismissRequest(
        @NotNull
        @Schema(description = "숨김 처리할 앱 버전 ID", example = "1")
        Long appVersionId
) {
}
