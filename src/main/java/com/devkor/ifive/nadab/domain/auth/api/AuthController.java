package com.devkor.ifive.nadab.domain.auth.api;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.AuthorizationUrlResponse;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.OAuth2LoginRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.response.TokenResponse;
import com.devkor.ifive.nadab.domain.auth.application.OAuth2Service;
import com.devkor.ifive.nadab.domain.auth.application.TokenService;
import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.infra.cookie.CookieManager;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        // Provider 검증 및 변환
        OAuth2Provider oauth2Provider = OAuth2Provider.fromString(provider);

        // OAuth2 인증 URL 생성 (state 포함)
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
        // Provider 검증 및 변환
        OAuth2Provider oauth2Provider = OAuth2Provider.fromString(provider);

        // Authorization Code로 토큰 발급 및 사용자 정보 조회
        TokenBundle tokenBundle = oauth2Service.executeOAuth2Login(oauth2Provider, request.code(), request.state());

        // Refresh Token을 HttpOnly 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

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

        // 토큰 재발급 (Rotation)
        TokenBundle tokenBundle = tokenService.refreshTokens(refreshToken);

        // 새로운 Refresh Token을 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

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

        // 쿠키에서 Refresh Token 제거
        cookieManager.removeRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }
}
