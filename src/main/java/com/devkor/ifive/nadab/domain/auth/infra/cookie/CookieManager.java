package com.devkor.ifive.nadab.domain.auth.infra.cookie;

import com.devkor.ifive.nadab.global.core.properties.TokenProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Refresh Token 쿠키 관리
 * - HttpOnly 쿠키 생성, 삭제, 추출
 */
@Component
@RequiredArgsConstructor
public class CookieManager {

    @Value("${app.cookie.secure}")
    private boolean isCookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    private static final String COOKIE_NAME = "refreshToken";

    private final TokenProperties tokenProperties;

    // Refresh Token HttpOnly 쿠키 생성
    private ResponseCookie create(String refreshToken) {
        long maxAge = tokenProperties.getRefreshTokenExpiration() / 1000; // 밀리초 → 초

        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isCookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    // Refresh Token 쿠키 삭제
    private ResponseCookie deleteWithPath(String path) {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isCookieSecure)
                .sameSite(cookieSameSite)
                .path(path)
                .maxAge(0) // 즉시 만료
                .build();
    }

    // Refresh Token을 쿠키로 생성하여 응답에 추가
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = create(refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());

        // [추가] 혹시 남아있을 수 있는 구버전 Path ('/api/v1/auth') 쿠키를 강제로 삭제
        ResponseCookie legacyCookie = deleteWithPath("/api/v1/auth");
        response.addHeader("Set-Cookie", legacyCookie.toString());
    }

    // Refresh Token 쿠키를 만료시켜 응답에 추가
    public void removeRefreshTokenCookie(HttpServletResponse response) {
        // 신규 path
        response.addHeader("Set-Cookie", deleteWithPath("/").toString());

        // 레거시 호환: 예전에 발급했던 path도 함께 삭제
        response.addHeader("Set-Cookie", deleteWithPath("/api/v1/auth").toString());
    }

    // 쿠키에서 Refresh Token 추출
    public String extract(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new UnauthorizedException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
    }
}