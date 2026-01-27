package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 검색한 유저 정보")
public record SearchHistoryResponse(
        @Schema(description = "닉네임", example = "춤추는사막여우")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/abc123.png")
        String profileImageUrl
) {
}