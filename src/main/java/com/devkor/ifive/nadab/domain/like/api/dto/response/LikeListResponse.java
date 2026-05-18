package com.devkor.ifive.nadab.domain.like.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "좋아요 리스트 응답")
public record LikeListResponse(

        @Schema(description = "좋아요 누른 사용자 목록 (최신순, 차단 관계 제외)")
        List<LikerResponse> likers
) {
}