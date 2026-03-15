package com.devkor.ifive.nadab.domain.auth.infra.oauth;

/**
 * OAuth2 사용자 정보
 * - OAuth2 제공자로부터 받은 사용자 정보 (providerId, email)
 */
public record OAuth2UserInfo(
        String providerId,
        String email
) {
}
