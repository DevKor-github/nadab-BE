package com.devkor.ifive.nadab.domain.ads.api.dto.response;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import io.swagger.v3.oas.annotations.media.Schema;

// 부족분 조회 응답
public record AdRewardQuoteResponse(
        @Schema(description = "대상 기능", example = "PDF_REPORT_AND_ANSWER")
        AdRewardFeature feature,

        @Schema(description = "기능 사용 비용(크리스탈)", example = "100")
        long crystalCost,

        @Schema(description = "현재 보유 크리스탈", example = "30")
        long balance,

        @Schema(description = "부족분 (광고로 채워야 할 크리스탈, 0 이상). 0이면 이미 충분해 광고 불필요", example = "70")
        long requiredCrystal
) {
}