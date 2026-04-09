package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.core.entity.ProviderType;
import com.devkor.ifive.nadab.domain.auth.core.entity.SocialAccount;
import com.devkor.ifive.nadab.domain.auth.core.entity.SocialAuthRefreshToken;
import com.devkor.ifive.nadab.domain.auth.core.repository.SocialAccountRepository;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.AppleOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.GoogleOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.KakaoOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.NaverOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.AppleTokenResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.security.crypto.DataCryptoService;
import com.devkor.ifive.nadab.global.security.crypto.EncryptedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Native SDK 소셜 로그인 서비스
 * - Android/iOS 앱에서 각 SDK로 받은 토큰으로 로그인 처리
 * - 네이버, 구글, 카카오, Apple 등 모든 Native SDK 로그인을 통합 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NativeAuthService {

    private final NaverOAuth2Client naverOAuth2Client;
    private final GoogleOAuth2Client googleOAuth2Client;
    private final KakaoOAuth2Client kakaoOAuth2Client;
    private final AppleOAuth2Client appleOAuth2Client;
    private final SocialAccountService socialAccountService;
    private final SocialAccountRepository socialAccountRepository;
    private final TokenService tokenService;
    private final DataCryptoService dataCryptoService;

    // 네이버 Native SDK 로그인 처리
    public TokenBundle executeNaverLogin(String naverAccessToken) {
        // 1. 네이버 API로 사용자 정보 조회
        OAuth2UserInfo userInfo = naverOAuth2Client.fetchUserInfo(naverAccessToken);

        // 2. 사용자 조회 또는 생성
        User user = socialAccountService.getOrCreateUser(
                OAuth2Provider.NAVER,
                userInfo.providerId(),
                userInfo.email()
        );

        // 3. JWT 토큰 발급
        return tokenService.issueTokens(user.getId());
    }

    // 구글 Native SDK 로그인 처리
    public TokenBundle executeGoogleLogin(String googleIdToken) {
        // 1. Google tokeninfo API로 ID Token 검증 및 사용자 정보 조회
        OAuth2UserInfo userInfo = googleOAuth2Client.verifyIdToken(googleIdToken);

        // 2. 사용자 조회 또는 생성
        User user = socialAccountService.getOrCreateUser(
                OAuth2Provider.GOOGLE,
                userInfo.providerId(),
                userInfo.email()
        );

        // 3. JWT 토큰 발급
        return tokenService.issueTokens(user.getId());
    }

    // 카카오 Native SDK 로그인 처리
    public TokenBundle executeKakaoLogin(String kakaoAccessToken) {
        // 1. 앱 키 검증 (다른 앱의 토큰 사용 방지)
        kakaoOAuth2Client.validateAccessTokenAppId(kakaoAccessToken);

        // 2. 카카오 API로 사용자 정보 조회
        OAuth2UserInfo userInfo = kakaoOAuth2Client.fetchUserInfo(kakaoAccessToken);

        // 3. 사용자 조회 또는 생성
        User user = socialAccountService.getOrCreateUser(
                OAuth2Provider.KAKAO,
                userInfo.providerId(),
                userInfo.email()
        );

        // 4. JWT 토큰 발급
        return tokenService.issueTokens(user.getId());
    }

    // 애플 Native SDK 로그인 처리 (iOS)
    public TokenBundle executeAppleLogin(String code) {
        // 1. Authorization Code → Access Token, Refresh Token, ID Token 교환
        AppleTokenResponse appleTokens = appleOAuth2Client.fetchTokensForNative(code);

        // 2. ID Token 파싱 및 사용자 정보 추출
        OAuth2UserInfo userInfo = appleOAuth2Client.parseIdToken(appleTokens.getIdToken());

        // 3. 사용자 조회 또는 생성
        User user = socialAccountService.getOrCreateUser(
                OAuth2Provider.APPLE,
                userInfo.providerId(),
                userInfo.email()
        );

        // 4. Refresh Token 암호화 저장
        if (appleTokens.getRefreshToken() != null) {
            ProviderType providerType = ProviderType.APPLE;
            SocialAccount socialAccount = socialAccountRepository
                    .findByProviderTypeAndProviderUserId(providerType, userInfo.providerId())
                    .orElseThrow(() -> new IllegalStateException("SocialAccount 조회 실패"));
            EncryptedPayload encrypted = dataCryptoService.encrypt(appleTokens.getRefreshToken().getBytes(UTF_8));
            socialAccount.updateRefreshToken(SocialAuthRefreshToken.from(encrypted));
        }

        // 5. JWT 토큰 발급
        return tokenService.issueTokens(user.getId());
    }
}