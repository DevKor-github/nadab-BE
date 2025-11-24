package com.devkor.ifive.nadab.domain.auth.api.dto.response;

/**
 * 토큰 응답 DTO (공통)
 * - OAuth2 로그인, 일반 로그인, 토큰 재발급 공통 응답
 * - Refresh Token은 HttpOnly 쿠키로 전달
 */
public record TokenResponse(
        String accessToken
) {
}
