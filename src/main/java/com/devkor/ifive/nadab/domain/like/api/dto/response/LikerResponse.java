package com.devkor.ifive.nadab.domain.like.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 누른 사용자")
public record LikerResponse(

        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "닉네임", example = "모래")
        String nickname,

        @Schema(description = "친구 여부 (true: 친구 삭제·차단 버튼 제공, false: 친구 신청 버튼 제공)")
        boolean isFriend
) {
}