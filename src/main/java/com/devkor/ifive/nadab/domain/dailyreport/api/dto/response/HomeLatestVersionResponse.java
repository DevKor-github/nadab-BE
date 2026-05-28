package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플랫폼별 최신 앱 버전")
public record HomeLatestVersionResponse(
        @Schema(description = "iOS 최신 앱 버전", example = "1.2.0", nullable = true)
        String ios,

        @Schema(description = "Android 최신 앱 버전", example = "1.2.0", nullable = true)
        String android,

        @Schema(description = "Web 최신 버전", example = "1.2.0", nullable = true)
        String web
) {
}
