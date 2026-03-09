package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.GoogleOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.KakaoOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.NaverOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.state.StateManager;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.OAuth2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 소셜 로그인 서비스
 * - Authorization URL 생성
 * - OAuth2 로그인 처리 (사용자 조회/생성 + 토큰 발급)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthService {

    private final NaverOAuth2Client naverOAuth2Client;
    private final GoogleOAuth2Client googleOAuth2Client;
    private final KakaoOAuth2Client kakaoOAuth2Client;
    private final StateManager stateManager;
    private final SocialAccountService socialAccountService;
    private final TokenService tokenService;

    // 프론트엔드에 전달할 Authorization URL 반환 (CSRF 방지를 위한 state 파라미터 포함)
    public String getAuthorizationUrl(OAuth2Provider provider) {
        // state 생성 및 저장
        String state = stateManager.generateAndStore();

        // provider에 따라 Authorization URL 생성
        return switch (provider) {
            case NAVER -> naverOAuth2Client.buildAuthorizationUrl(state);
            case GOOGLE -> googleOAuth2Client.buildAuthorizationUrl(state);
            case KAKAO -> kakaoOAuth2Client.buildAuthorizationUrl(state);
        };
    }

    // OAuth2 로그인 처리(Authorization Code → Access Token → 사용자 정보 → 토큰 발급)
    @Transactional
    public TokenBundle executeOAuth2Login(OAuth2Provider provider, String code, String state) {
        // 1. State 검증
        if (!stateManager.validateAndRemove(state)) {
            log.warn("OAuth2 state 검증 실패");
            throw new OAuth2Exception(ErrorCode.AUTH_INVALID_STATE);
        }

        // 2. Access Token 발급
        String accessToken = switch (provider) {
            case NAVER -> naverOAuth2Client.fetchAccessToken(code, state);
            case GOOGLE -> googleOAuth2Client.fetchAccessToken(code);
            case KAKAO -> kakaoOAuth2Client.fetchAccessToken(code);
        };

        // 3. 사용자 정보 조회
        OAuth2UserInfo userInfo = switch (provider) {
            case NAVER -> naverOAuth2Client.fetchUserInfo(accessToken);
            case GOOGLE -> googleOAuth2Client.fetchUserInfo(accessToken);
            case KAKAO -> kakaoOAuth2Client.fetchUserInfo(accessToken);
        };

        // 4. 사용자 조회 또는 생성
        User user = socialAccountService.getOrCreateUser(provider, userInfo.providerId(), userInfo.email());

        // 5. 토큰 발급 (Access Token + Refresh Token)
        return tokenService.issueTokens(user.getId());
    }
}