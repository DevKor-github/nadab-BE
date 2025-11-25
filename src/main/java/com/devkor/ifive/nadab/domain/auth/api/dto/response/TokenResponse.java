package com.devkor.ifive.nadab.domain.auth.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 응답 DTO (공통)
 * - OAuth2 로그인, 일반 로그인, 토큰 재발급 공통 응답
 * - Refresh Token은 HttpOnly 쿠키로 전달
 */
@Schema(description = "인증 토큰 응답 (Access Token과 signupStatus는 응답 바디, Refresh Token은 HttpOnly 쿠키)")
public record TokenResponse(
        @Schema(description = "JWT Access Token (유효기간: 1시간)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        String accessToken,

        @Schema(description = "계정 상태", example = "PROFILE_INCOMPLETE")
        String signupStatus
) {
}
