package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MonthlySocialRankingItemResponse(
        @Schema(description = "화면 표시 순서", example = "1")
        int displayOrder,

        @Schema(description = "친구 사용자 ID", example = "12")
        Long userId,

        @Schema(description = "친구 닉네임", example = "가나다")
        String nickname,

        @Schema(description = "친구 프로필 이미지 URL", nullable = true)
        String profileImageUrl,

        @Schema(description = "공동 1위를 포함한 1위 강조 여부", example = "true")
        boolean topRank
) {
}
