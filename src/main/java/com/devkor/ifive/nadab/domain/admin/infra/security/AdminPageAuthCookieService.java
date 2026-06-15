package com.devkor.ifive.nadab.domain.admin.infra.security;

import com.devkor.ifive.nadab.domain.admin.core.properties.AdminPageProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminPageAuthCookieService {

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    private final AdminPageProperties adminPageProperties;

    public void addCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(adminPageProperties.getCookieName(), token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/admin")
                .maxAge(adminPageProperties.getTokenExpirationSeconds())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void expireCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(adminPageProperties.getCookieName(), "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/admin")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
