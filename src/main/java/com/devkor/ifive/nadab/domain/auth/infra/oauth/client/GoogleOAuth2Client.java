package com.devkor.ifive.nadab.domain.auth.infra.oauth.client;

import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.GoogleTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.GoogleProfileResponse;
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
}