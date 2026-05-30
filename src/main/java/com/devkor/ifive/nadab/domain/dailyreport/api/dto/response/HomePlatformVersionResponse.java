package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플랫폼별 최신 버전 정보")
public record HomePlatformVersionResponse(
        @Schema(description = "앱 버전 ID", example = "1")
        Long appVersionId,

        @Schema(description = "최신 앱 버전", example = "1.2.0")
        String version,

        @Schema(description = "업데이트 요약 문장", example = "좋아요와 댓글로 마음을 전해요.")
        String summary,

        @Schema(description = "업데이트 항목 목록")
        List<HomeVersionItemResponse> items,

        @Schema(description = "다시 보지 않기 여부", example = "false")
        boolean dismissed
) {
}
