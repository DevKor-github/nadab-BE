package com.devkor.ifive.nadab.domain.auth.api.dto;

/**
 * OAuth2 로그인 요청 DTO
 * - 프론트엔드가 Authorization Code와 State를 전달
 */
public record OAuth2LoginRequest(
        String code,
        String state
) {
}
