package com.devkor.ifive.nadab.domain.auth.infra.oauth.client;

import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.KakaoTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.KakaoProfileResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.KakaoAccessTokenInfoResponse;
import com.devkor.ifive.nadab.global.core.properties.KakaoProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.OAuth2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 카카오 OAuth2 인증 플로우 처리
 * - Authorization URL 생성, Access Token 발급, 사용자 정보 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuth2Client {
    private static final String AUTHORIZATION_URI = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final KakaoProperties kakaoProperties;
    private final WebClient webClient;

    // Authorization URL 생성 (프론트 전달용)
    public String buildAuthorizationUrl(String state) {
        return AUTHORIZATION_URI +
                "?client_id=" + kakaoProperties.getClientId() +
                "&redirect_uri=" + kakaoProperties.getRedirectUri() +
                "&response_type=code" +
                "&state=" + state +
                "&prompt=select_account";  // 매번 계정 선택 화면 표시
    }

    // Access Token 발급
    public String fetchAccessToken(String code) {
        KakaoTokenResponse response = webClient.post()
                .uri(TOKEN_URI)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", kakaoProperties.getClientId())
                        .with("client_secret", kakaoProperties.getClientSecret())
                        .with("code", code)
                        .with("redirect_uri", kakaoProperties.getRedirectUri()))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(KakaoTokenResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getErrorDescription() != null
                                            ? errorResponse.getErrorDescription()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("카카오 토큰 발급 실패: {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
                                }))
                .bodyToMono(KakaoTokenResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            log.warn("카카오 토큰 발급 실패: 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        // 에러 응답 체크 (HTTP 200이지만 error 필드가 있는 경우)
        if (response.getError() != null) {
            String errorMessage = response.getErrorDescription() != null
                    ? response.getErrorDescription()
                    : "알 수 없는 오류";
            log.warn("카카오 토큰 발급 실패: {}", errorMessage);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        // Access Token 체크 (error가 null인 정상 응답인데 access_token이 없는 예외 상황 방어)
        if (response.getAccessToken() == null) {
            log.warn("카카오 토큰 발급 실패: access_token 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        return response.getAccessToken();
    }

    // 사용자 정보 조회
    public OAuth2UserInfo fetchUserInfo(String accessToken) {
        KakaoProfileResponse response = webClient.get()
                .uri(USER_INFO_URI)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(KakaoProfileResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getMsg() != null
                                            ? errorResponse.getMsg()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("카카오 사용자 정보 조회 실패: {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
                                }))
                .bodyToMono(KakaoProfileResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            log.warn("카카오 사용자 정보 조회 실패: 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 필수 필드 체크
        if (response.getId() == null) {
            log.warn("카카오 사용자 정보 조회 실패: id 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // kakao_account 체크
        if (response.getKakaoAccount() == null) {
            log.warn("카카오 사용자 정보 조회 실패: kakao_account 정보 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 이메일 동의 여부 체크
        if (Boolean.TRUE.equals(response.getKakaoAccount().getEmailNeedsAgreement())) {
            log.warn("카카오 사용자 정보 조회 실패: 이메일 제공 동의 필요");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 이메일 존재 여부 체크
        if (response.getKakaoAccount().getEmail() == null) {
            log.warn("카카오 사용자 정보 조회 실패: 이메일 정보 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 이메일 유효성 체크
        if (Boolean.FALSE.equals(response.getKakaoAccount().getIsEmailValid())) {
            log.warn("카카오 사용자 정보 조회 실패: 유효하지 않은 이메일 (다른 계정에 이미 연결됨)");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 이메일 인증 여부 체크
        if (Boolean.FALSE.equals(response.getKakaoAccount().getIsEmailVerified())) {
            log.warn("카카오 사용자 정보 조회 실패: 인증되지 않은 이메일");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        return new OAuth2UserInfo(
                String.valueOf(response.getId()),
                response.getKakaoAccount().getEmail()
        );
    }

    // Access Token 앱 키 검증 (Native SDK 로그인용, 다른 앱의 토큰 사용 방지)
    public void validateAccessTokenAppId(String accessToken) {
        KakaoAccessTokenInfoResponse response = webClient.get()
                .uri("https://kapi.kakao.com/v1/user/access_token_info")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(KakaoAccessTokenInfoResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getMsg() != null
                                            ? errorResponse.getMsg()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("카카오 Access Token 검증 실패: {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
                                }))
                .bodyToMono(KakaoAccessTokenInfoResponse.class)
                .block();

        // 응답 없음
        if (response == null) {
            log.warn("카카오 Access Token 검증 실패: 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 에러 응답 체크
        if (response.getMsg() != null) {
            String errorMessage = response.getMsg();
            log.warn("카카오 Access Token 검증 실패: {}", errorMessage);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 앱 ID 체크
        if (response.getAppId() == null) {
            log.warn("카카오 Access Token 검증 실패: app_id 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 앱 ID 검증 (우리 앱의 토큰인지 확인)
        if (!kakaoProperties.getAppId().equals(response.getAppId())) {
            log.warn("카카오 Access Token 검증 실패: 다른 앱의 토큰 (기대: {}, 실제: {})",
                    kakaoProperties.getAppId(), response.getAppId());
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        log.debug("카카오 Access Token 검증 성공: app_id={}", response.getAppId());
    }
}