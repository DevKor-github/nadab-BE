package com.devkor.ifive.nadab.domain.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "유저 프로필 정보 응답")
public record UserProfileResponse(
    String nickname,
    String email,
    String profileImageUrl,
    String interestCode,
    LocalDate registeredDate
) {
}
