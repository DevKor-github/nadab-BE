package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플랫폼별 최신 앱 버전")
public record HomeLatestVersionResponse(
        HomePlatformVersionResponse ios,

        HomePlatformVersionResponse android
) {
}
