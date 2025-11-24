package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * OAuth2 로그인 요청 DTO
 * - 프론트엔드가 Authorization Code와 State를 전달
 */
public record OAuth2LoginRequest(
        @NotBlank(message = "Authorization code는 필수입니다")
        String code,

        @NotBlank(message = "State는 필수입니다")
        String state
) {
}