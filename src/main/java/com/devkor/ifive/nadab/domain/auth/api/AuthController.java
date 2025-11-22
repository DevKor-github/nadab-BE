package com.devkor.ifive.nadab.domain.auth.api;

import com.devkor.ifive.nadab.domain.auth.api.dto.AuthorizationUrlResponse;
import com.devkor.ifive.nadab.domain.auth.api.dto.OAuth2LoginRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.TokenResponse;
import com.devkor.ifive.nadab.domain.auth.application.OAuth2Service;
import com.devkor.ifive.nadab.domain.auth.application.TokenService;
import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.infra.cookie.CookieManager;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 통합 컨트롤러
 * - OAuth2 로그인
 * - 일반 로그인
 * - 공통 API (토큰 재발급, 로그아웃)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oauth2Service;
    private final TokenService tokenService;
    private final CookieManager cookieManager;

    // Authorization URL 조회
    @GetMapping("/{provider}/url")
    @PermitAll
    public ResponseEntity<AuthorizationUrlResponse> getAuthorizationUrl(
            @PathVariable("provider") String provider
    ) {
        OAuth2Provider oauth2Provider = OAuth2Provider.valueOf(provider.toUpperCase());
        String authorizationUrl = oauth2Service.getAuthorizationUrl(oauth2Provider);

        return ResponseEntity.ok(new AuthorizationUrlResponse(authorizationUrl));
    }

    // OAuth2 로그인
    @PostMapping("/{provider}/login")
    @PermitAll
    public ResponseEntity<TokenResponse> oauth2Login(
            @PathVariable("provider") String provider,
            @RequestBody OAuth2LoginRequest request,
            HttpServletResponse response
    ) {
        OAuth2Provider oauth2Provider = OAuth2Provider.valueOf(provider.toUpperCase());
        TokenBundle tokenBundle = oauth2Service.executeOAuth2Login(
                oauth2Provider,
                request.code(),
                request.state()
        );

        // Refresh Token을 HttpOnly 쿠키에 저장
        ResponseCookie cookie = cookieManager.create(tokenBundle.refreshToken());
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new TokenResponse(tokenBundle.accessToken()));
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    @PermitAll
    public ResponseEntity<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = cookieManager.extract(request);
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh Token이 없습니다.");
        }

        // 토큰 재발급 (Rotation)
        TokenBundle tokenBundle = tokenService.refreshTokens(refreshToken);

        // 새로운 Refresh Token을 HttpOnly 쿠키에 저장
        ResponseCookie cookie = cookieManager.create(tokenBundle.refreshToken());
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new TokenResponse(tokenBundle.accessToken()));
    }

    // 로그아웃
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // DB에서 Refresh Token 삭제
        tokenService.revokeTokens(principal.getId());

        // 쿠키 삭제
        ResponseCookie cookie = cookieManager.delete();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.noContent().build();
    }
}
