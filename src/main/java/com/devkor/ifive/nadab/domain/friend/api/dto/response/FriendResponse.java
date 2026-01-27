package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 프로필 정보")
public record FriendResponse(
        @Schema(description = "친구 관계 ID", example = "123")
        Long friendshipId,

        @Schema(description = "닉네임", example = "모래")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/abc123.png")
        String profileImageUrl,

        @Schema(description = "탈퇴 예정 여부 (14일 유예기간 중)", example = "false")
        Boolean isWithdrawn
) {
}