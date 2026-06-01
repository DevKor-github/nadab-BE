package com.devkor.ifive.nadab.domain.admin.infra.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminPageAuthInterceptor implements HandlerInterceptor {

    private final AdminPageAuthTokenService adminPageAuthTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        String token = extractCookieValue(request, adminPageAuthTokenService.getCookieName());
        boolean isTokenValid = adminPageAuthTokenService.isValid(token);

        if (isLoginApi(requestUri)) {
            return true;
        }

        if (isLoginPage(requestUri)) {
            if (isTokenValid) {
                response.sendRedirect("/admin");
                return false;
            }
            return true;
        }

        if (isTokenValid) {
            return true;
        }

        if (requestUri.startsWith("/admin/api/")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        response.sendRedirect("/admin/login");
        return false;
    }

    private boolean isLoginApi(String requestUri) {
        return "/admin/api/login".equals(requestUri);
    }

    private boolean isLoginPage(String requestUri) {
        return "/admin/login".equals(requestUri) || requestUri.startsWith("/admin/login/");
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
