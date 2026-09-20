package com.devkor.ifive.nadab.domain.ads.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

// 세션 발급(commit) 응답. sessionKey를 AdMob custom_data로 실어 광고를 띄운다.
public record AdRewardSessionCreateResponse(
        @Schema(description = "세션 토큰(AdMob custom_data로 전달)", example = "aB3x... (40자)")
        String sessionKey,

        @Schema(description = "지급 예정 크리스탈 N(확정값)", example = "70")
        long rewardAmount,

        @Schema(description = "세션 만료 시각", example = "2026-09-19T12:10:00Z")
        OffsetDateTime expiresAt
) {
}