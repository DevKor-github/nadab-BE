package com.devkor.ifive.nadab.domain.auth.infra.oauth;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;

/**
 * OAuth2 제공자 (Provider)
 * - 지원되는 소셜 로그인 제공자를 정의
 */
public enum OAuth2Provider {
    NAVER,
    GOOGLE;

    public static OAuth2Provider fromString(String provider) {
        for (OAuth2Provider p : values()) {
            if (p.name().equalsIgnoreCase(provider)) {
                return p;
            }
        }
        throw new BadRequestException(ErrorCode.AUTH_UNSUPPORTED_OAUTH2_PROVIDER);
    }
}
