package com.devkor.ifive.nadab.domain.auth.infra.cookie;

import com.devkor.ifive.nadab.global.core.properties.TokenProperties;
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

    private static final String COOKIE_NAME = "refreshToken";

    private final TokenProperties tokenProperties;

    // Refresh Token HttpOnly 쿠키 생성
    private ResponseCookie create(String refreshToken) {
        long maxAge = tokenProperties.getRefreshTokenExpiration() / 1000; // 밀리초 → 초

        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isCookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }

    // Refresh Token 쿠키 삭제
    private ResponseCookie delete() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isCookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0) // 즉시 만료
                .build();
    }

    // Refresh Token을 쿠키로 생성하여 응답에 추가
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = create(refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    // Refresh Token 쿠키를 만료시켜 응답에 추가
    public void removeRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = delete();
        response.addHeader("Set-Cookie", cookie.toString());
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
        throw new UnauthorizedException("Refresh Token이 없습니다.");
    }
}