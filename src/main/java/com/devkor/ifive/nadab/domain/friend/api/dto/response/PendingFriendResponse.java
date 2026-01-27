package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 요청 정보")
public record PendingFriendResponse(
        @Schema(description = "친구 관계 ID", example = "123")
        Long friendshipId,

        @Schema(description = "닉네임", example = "춤추는사막여우")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/abc123.png")
        String profileImageUrl
) {
}