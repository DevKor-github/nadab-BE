package com.devkor.ifive.nadab.domain.auth.infra.oauth.client;

import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.GoogleTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.GoogleProfileResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.GoogleIdTokenInfoResponse;
import com.devkor.ifive.nadab.global.core.properties.GoogleProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.OAuth2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 구글 OAuth2 인증 플로우 처리
 * Authorization URL 생성, Access Token 발급, 사용자 정보 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2Client {
    private static final String AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String TOKEN_INFO_URI = "https://oauth2.googleapis.com/tokeninfo";
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String GOOGLE_ISSUER_ALT = "accounts.google.com";

    private final GoogleProperties googleProperties;
    private final WebClient webClient;

    // Authorization URL 생성(프론트 전달용)
    public String buildAuthorizationUrl(String state) {
        return AUTHORIZATION_URI +
                "?client_id=" + googleProperties.getClientId() +
                "&redirect_uri=" + googleProperties.getRedirectUri() +
                "&response_type=code" +
                "&scope=openid%20email" +
                "&state=" + state +
                "&prompt=select_account";  // 매번 계정 선택 화면 표시
    }

    // Access Token 발급
    public String fetchAccessToken(String code) {
        GoogleTokenResponse response = webClient.post()
                .uri(TOKEN_URI)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", googleProperties.getClientId())
                        .with("client_secret", googleProperties.getClientSecret())
                        .with("code", code)
                        .with("redirect_uri", googleProperties.getRedirectUri()))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(GoogleTokenResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getErrorDescription() != null
                                            ? errorResponse.getErrorDescription()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("구글 토큰 발급 실패: {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
                                }))
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            log.warn("구글 토큰 발급 실패: 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        // 에러 응답 체크 (HTTP 200이지만 error 필드가 있는 경우)
        if (response.getError() != null) {
            String errorMessage = response.getErrorDescription() != null
                    ? response.getErrorDescription()
                    : "알 수 없는 오류";
            log.warn("구글 토큰 발급 실패: {}", errorMessage);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        // Access Token 체크 (error가 null인 정상 응답인데 access_token이 없는 예외 상황 방어)
        if (response.getAccessToken() == null) {
            log.warn("구글 토큰 발급 실패: access_token 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        return response.getAccessToken();
    }

    // 사용자 정보 조회
    public OAuth2UserInfo fetchUserInfo(String accessToken) {
        GoogleProfileResponse response = webClient.get()
                .uri(USER_INFO_URI)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(GoogleProfileResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getErrorDescription() != null
                                            ? errorResponse.getErrorDescription()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("구글 사용자 정보 조회 실패: {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
                                }))
                .bodyToMono(GoogleProfileResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            log.warn("구글 사용자 정보 조회 실패: 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 에러 응답 체크 (HTTP 200이지만 error 필드가 있는 경우)
        if (response.getError() != null) {
            String errorMessage = response.getErrorDescription() != null
                    ? response.getErrorDescription()
                    : "알 수 없는 오류";
            log.warn("구글 사용자 정보 조회 실패: {}", errorMessage);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 필수 필드 체크
        if (response.getSub() == null || response.getEmail() == null) {
            log.warn("구글 사용자 정보 조회 실패: 필수 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 이메일 인증 여부 체크
        if (!response.getEmailVerified()) {
            log.warn("구글 사용자 정보 조회 실패: 인증되지 않은 이메일");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        return new OAuth2UserInfo(
                response.getSub(),
                response.getEmail()
        );
    }

    // ID Token 검증 (Native SDK 로그인용)
    public OAuth2UserInfo verifyIdToken(String googleIdToken) {
        // 1. Google tokeninfo API 호출
        GoogleIdTokenInfoResponse response = webClient.get()
                .uri(TOKEN_INFO_URI + "?id_token={idToken}", googleIdToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(GoogleIdTokenInfoResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getErrorDescription() != null
                                            ? errorResponse.getErrorDescription()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("구글 ID Token 검증 실패: {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
                                }))
                .bodyToMono(GoogleIdTokenInfoResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            log.warn("구글 ID Token 검증 실패: 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 에러 응답 체크
        if (response.getError() != null) {
            String errorMessage = response.getErrorDescription() != null
                    ? response.getErrorDescription()
                    : "알 수 없는 오류";
            log.warn("구글 ID Token 검증 실패: {}", errorMessage);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 2. 토큰 정보 검증
        validateIdTokenInfo(response);

        // 3. 사용자 정보 반환
        return new OAuth2UserInfo(
                response.getSub(),
                response.getEmail()
        );
    }

    // ID Token 정보 검증
    private void validateIdTokenInfo(GoogleIdTokenInfoResponse tokenInfo) {
        // 필수 필드 체크
        if (tokenInfo.getSub() == null || tokenInfo.getEmail() == null) {
            log.warn("구글 ID Token 검증 실패: 필수 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // issuer 검증
        if (!GOOGLE_ISSUER.equals(tokenInfo.getIss()) && !GOOGLE_ISSUER_ALT.equals(tokenInfo.getIss())) {
            log.warn("구글 ID Token 검증 실패: 유효하지 않은 issuer - {}", tokenInfo.getIss());
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // audience 검증 (우리 앱의 Web Client ID인지)
        if (!googleProperties.getClientId().equals(tokenInfo.getAud())) {
            log.warn("구글 ID Token 검증 실패: 유효하지 않은 audience - {}", tokenInfo.getAud());
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // azp (Authorized party) 검증 (azp가 있으면 우리 앱의 Android Client ID인지 확인)
        if (tokenInfo.getAzp() != null && !googleProperties.getAndroidClientId().equals(tokenInfo.getAzp())) {
            log.warn("구글 ID Token 검증 실패: 유효하지 않은 authorized party - {}", tokenInfo.getAzp());
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 만료 시간 검증
        long currentTime = System.currentTimeMillis() / 1000;
        if (tokenInfo.getExp() != null && tokenInfo.getExp() < currentTime) {
            log.warn("구글 ID Token 검증 실패: 토큰 만료");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 이메일 인증 확인
        if (tokenInfo.getEmailVerified() == null || !tokenInfo.getEmailVerified()) {
            log.warn("구글 ID Token 검증 실패: 인증되지 않은 이메일");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }
    }
}