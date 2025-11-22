package com.devkor.ifive.nadab.domain.auth.infra.oauth.client;

import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.NaverTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.NaverProfileResponse;
import com.devkor.ifive.nadab.global.core.properties.NaverProperties;
import com.devkor.ifive.nadab.global.exception.OAuth2Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 네이버 OAuth2 인증 플로우 처리
 * - Authorization URL 생성, Access Token 발급, 사용자 정보 조회
 */
@Component
@RequiredArgsConstructor
public class NaverOAuth2Client {
    private static final String AUTHORIZATION_URI = "https://nid.naver.com/oauth2.0/authorize";
    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final NaverProperties naverProperties;
    private final WebClient webClient;

    // Authorization URL 생성 (프론트 전달용)
    public String buildAuthorizationUrl(String state) {
        return AUTHORIZATION_URI +
                "?response_type=code" +
                "&client_id=" + naverProperties.getClientId() +
                "&redirect_uri=" + naverProperties.getRedirectUri() +
                "&state=" + state;
    }

    // Access Token 발급
    public String fetchAccessToken(String code, String state) {
        NaverTokenResponse response = webClient.post()
                .uri(TOKEN_URI)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", naverProperties.getClientId())
                        .with("client_secret", naverProperties.getClientSecret())
                        .with("code", code)
                        .with("state", state))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(NaverTokenResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getErrorDescription() != null
                                            ? errorResponse.getErrorDescription()
                                            : "HTTP " + clientResponse.statusCode();
                                    return new OAuth2Exception("네이버 토큰 발급 실패: " + errorMessage, 401);
                                }))
                .bodyToMono(NaverTokenResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            throw new OAuth2Exception("네이버 토큰 발급 실패: 응답 없음", 401);
        }

        // 에러 응답 체크 (HTTP 200이지만 error 필드가 있는 경우)
        if (response.getError() != null) {
            String errorMessage = response.getErrorDescription() != null
                    ? response.getErrorDescription()
                    : "알 수 없는 오류";
            throw new OAuth2Exception("네이버 토큰 발급 실패: " + errorMessage, 401);
        }

        // Access Token 체크 (error가 null인 정상 응답인데 access_token이 없는 예외 상황 방어)
        if (response.getAccessToken() == null) {
            throw new OAuth2Exception("네이버 토큰 발급 실패: access_token 필드 없음", 401);
        }

        return response.getAccessToken();
    }

    // 사용자 정보 조회
    public OAuth2UserInfo fetchUserInfo(String accessToken) {
        NaverProfileResponse response = webClient.get()
                .uri(USER_INFO_URI)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(NaverProfileResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getMessage() != null
                                            ? errorResponse.getMessage()
                                            : "HTTP " + clientResponse.statusCode();
                                    return new OAuth2Exception("네이버 사용자 정보 조회 실패: " + errorMessage, 401);
                                }))
                .bodyToMono(NaverProfileResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            throw new OAuth2Exception("네이버 사용자 정보 조회 실패: 응답 없음", 401);
        }

        // result code 체크 ("00"이 성공, HTTP 200이지만 resultcode가 "00"이 아닌 경우)
        if (!"00".equals(response.getResultcode())) {
            String errorMessage = response.getMessage() != null
                    ? response.getMessage()
                    : "알 수 없는 오류";
            throw new OAuth2Exception("네이버 사용자 정보 조회 실패: " + errorMessage, 401);
        }

        // response 필드 체크
        NaverProfileResponse.ProfileData profileData = response.getResponse();
        if (profileData == null) {
            throw new OAuth2Exception("네이버 사용자 정보 조회 실패: response 필드 없음", 401);
        }

        // 필수 필드 체크
        if (profileData.getEmail() == null || profileData.getId() == null) {
            throw new OAuth2Exception("네이버 사용자 정보 조회 실패: 필수 필드 없음", 401);
        }

        return new OAuth2UserInfo(
                profileData.getId(),
                profileData.getEmail()
        );
    }
}