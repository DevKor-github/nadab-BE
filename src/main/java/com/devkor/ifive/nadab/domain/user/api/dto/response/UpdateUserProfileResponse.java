package com.devkor.ifive.nadab.domain.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 프로필 수정 응답")
public record UpdateUserProfileResponse(
        String nickname,
        String email,
        String profileImageUrl
) {
}
