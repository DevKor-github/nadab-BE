package com.devkor.ifive.nadab.domain.moderation.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "차단 사용자 정보")
public record BlockedUserResponse(
        @Schema(description = "차단 관계 ID", example = "12")
        Long userBlockId,

        @Schema(description = "닉네임", example = "모래")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/abc123.png")
        String profileImageUrl
) {
}
