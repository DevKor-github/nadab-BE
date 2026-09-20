package com.devkor.ifive.nadab.domain.ads.api.dto.response;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 폴링 응답. status는 계산값(TTL 지나면 EXPIRED).
public record AdRewardSessionStatusResponse(
        @Schema(description = "세션 상태(PENDING 대기 / REWARDED 지급완료 / EXPIRED 만료)", example = "REWARDED")
        AdRewardSessionStatus status,

        @Schema(description = "지급 예정/완료 크리스탈 N", example = "70")
        long rewardAmount
) {
}