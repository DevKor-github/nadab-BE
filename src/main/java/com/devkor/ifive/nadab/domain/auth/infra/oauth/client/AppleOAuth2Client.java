package com.devkor.ifive.nadab.domain.auth.infra.oauth.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.AppleTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto.AppleIdTokenPayload;
import com.devkor.ifive.nadab.global.core.properties.AppleProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.OAuth2Exception;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * 애플 OAuth2 인증 플로우 처리 (iOS Native 전용)
 * - Authorization Code → Token 교환
 * - ID Token 파싱 및 검증
 * - Refresh Token Revoke
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuth2Client {
    private static final String TOKEN_URI = "https://appleid.apple.com/auth/token";
    private static final String REVOKE_URI = "https://appleid.apple.com/auth/revoke";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final long CLIENT_SECRET_EXPIRATION_SECONDS = 15777000L; // 6개월

    private final AppleProperties appleProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Authorization Code → Access Token, Refresh Token, ID Token 교환
    public AppleTokenResponse fetchTokensForNative(String code) {
        String clientSecret = generateClientSecret(appleProperties.getClientId());

        AppleTokenResponse response = webClient.post()
                .uri(TOKEN_URI)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("client_id", appleProperties.getClientId())
                        .with("client_secret", clientSecret)
                        .with("grant_type", "authorization_code")
                        .with("code", code))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(AppleTokenResponse.class)
                                .map(errorResponse -> {
                                    String errorMessage = errorResponse.getErrorDescription() != null
                                            ? errorResponse.getErrorDescription()
                                            : "HTTP " + clientResponse.statusCode();
                                    log.warn("애플 토큰 발급 실패 (Native): {}", errorMessage);
                                    return new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
                                }))
                .bodyToMono(AppleTokenResponse.class)
                .block();

        // 응답 검증
        if (response == null) {
            log.warn("애플 토큰 발급 실패 (Native): 응답 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        if (response.getError() != null) {
            String errorMessage = response.getErrorDescription() != null
                    ? response.getErrorDescription()
                    : "알 수 없는 오류";
            log.warn("애플 토큰 발급 실패 (Native): {}", errorMessage);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        if (response.getIdToken() == null) {
            log.warn("애플 토큰 발급 실패 (Native): id_token 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }

        return response;
    }

    // ID Token 파싱 및 검증
    public OAuth2UserInfo parseIdToken(String idToken) {
        try {
            // 1. JWT 디코딩 (서명 검증은 생략, 애플 공개 키로 검증하려면 별도 구현 필요)
            DecodedJWT jwt = JWT.decode(idToken);

            // 2. Payload 파싱
            String payloadJson = new String(Base64.getUrlDecoder().decode(jwt.getPayload()));
            AppleIdTokenPayload payload = objectMapper.readValue(payloadJson, AppleIdTokenPayload.class);

            // 3. 클레임 검증
            validateIdTokenPayload(payload);

            // 4. 사용자 정보 반환
            return new OAuth2UserInfo(
                    payload.getSub(),
                    payload.getEmail()
            );
        } catch (OAuth2Exception e) {
            throw e;
        } catch (Exception e) {
            log.warn("애플 ID Token 파싱 실패", e);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }
    }

    // Refresh Token Revoke (회원 탈퇴 시, iOS Native 전용)
    public void revokeToken(String refreshToken) {
        try {
            String clientSecret = generateClientSecret(appleProperties.getClientId());

            webClient.post()
                    .uri(REVOKE_URI)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData("client_id", appleProperties.getClientId())
                            .with("client_secret", clientSecret)
                            .with("token", refreshToken)
                            .with("token_type_hint", "refresh_token"))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("애플 Refresh Token revoke 성공");
        } catch (Exception e) {
            log.warn("애플 Refresh Token revoke 실패 (무시하고 계속): {}", e.getMessage());
        }
    }

    private String generateClientSecret(String clientId) {
        try {
            // 1. Private Key 파싱
            ECPrivateKey privateKey = parsePrivateKey(appleProperties.getPrivateKey());

            // 2. ES256 알고리즘 생성
            Algorithm algorithm = Algorithm.ECDSA256(null, privateKey);

            // 3. JWT 생성
            Instant now = Instant.now();
            return JWT.create()
                    .withKeyId(appleProperties.getKeyId())              // Header: kid
                    .withIssuer(appleProperties.getTeamId())            // Payload: iss
                    .withIssuedAt(Date.from(now))                       // Payload: iat
                    .withExpiresAt(Date.from(now.plusSeconds(CLIENT_SECRET_EXPIRATION_SECONDS)))  // Payload: exp
                    .withAudience(APPLE_ISSUER)                         // Payload: aud
                    .withSubject(clientId)                              // Payload: sub (App ID 또는 Service ID)
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("애플 Client Secret 생성 실패", e);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }
    }

    private ECPrivateKey parsePrivateKey(String privateKeyContent) {
        try {
            if (privateKeyContent == null || privateKeyContent.trim().isEmpty()) {
                throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
            }

            // 1. 리터럴 \n을 실제 개행으로 변환
            String normalizedKey = privateKeyContent.replace("\\n", "\n");

            // 2. PEM 헤더/푸터 제거
            String privateKeyPEM = normalizedKey
                    .replaceAll("-+BEGIN[A-Z ]*PRIVATE KEY-+", "")
                    .replaceAll("-+END[A-Z ]*PRIVATE KEY-+", "")
                    .replaceAll("\\s", "");

            // 3. Base64 디코딩
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            // 4. PKCS8 형식으로 파싱
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("애플 Private Key 파싱 실패", e);
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_TOKEN_FAILED);
        }
    }

    private void validateIdTokenPayload(AppleIdTokenPayload payload) {
        // 필수 필드 체크 (sub 필수)
        if (payload.getSub() == null) {
            log.warn("애플 ID Token 검증 실패: sub 필드 없음");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // issuer 검증
        if (!APPLE_ISSUER.equals(payload.getIss())) {
            log.warn("애플 ID Token 검증 실패: 유효하지 않은 issuer - {}", payload.getIss());
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // audience 검증 (App ID, iOS Native 전용)
        if (!appleProperties.getClientId().equals(payload.getAud())) {
            log.warn("애플 ID Token 검증 실패: 유효하지 않은 audience - {}", payload.getAud());
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }

        // 만료 시간 검증
        long currentTime = System.currentTimeMillis() / 1000;
        if (payload.getExp() != null && payload.getExp() < currentTime) {
            log.warn("애플 ID Token 검증 실패: 토큰 만료");
            throw new OAuth2Exception(ErrorCode.AUTH_OAUTH2_USERINFO_FAILED);
        }
    }
}