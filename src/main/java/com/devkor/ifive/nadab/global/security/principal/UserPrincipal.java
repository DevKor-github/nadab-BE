package com.devkor.ifive.nadab.global.security.principal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security 인증 주체(Principal)
 * - Authentication 객체에 담기는 사용자 정보
 * - Controller에서 @AuthenticationPrincipal 사용
 */
@RequiredArgsConstructor
@Getter
public class UserPrincipal {
    private final Long id;
}
