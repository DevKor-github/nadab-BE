package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MonthlySocialSummaryResponse(
        @Schema(description = "월간 소셜 페이지 노출 여부", example = "true")
        boolean visible,

        @Schema(description = "집계 대상 월", example = "5")
        int month,

        @Schema(description = "내 DailyReport에 좋아요를 많이 누른 친구 랭킹")
        List<MonthlySocialRankingItemResponse> likeRanking,

        @Schema(description = "내 DailyReport에 댓글·대댓글을 많이 작성한 친구 랭킹")
        List<MonthlySocialRankingItemResponse> commentRanking
) {
}
