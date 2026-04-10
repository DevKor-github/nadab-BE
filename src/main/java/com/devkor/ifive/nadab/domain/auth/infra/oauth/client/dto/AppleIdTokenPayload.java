package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 애플 ID Token 페이로드 (JWT 디코딩 후)
 * - JWT 형식: Header.Payload.Signature
 * - Payload 부분을 Base64 디코딩한 결과
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleIdTokenPayload {
    @JsonProperty("iss")
    private String iss;             // 발급자: https://appleid.apple.com

    @JsonProperty("aud")
    private String aud;             // Audience: Bundle ID 또는 Service ID

    @JsonProperty("exp")
    private Long exp;               // 만료 시간 (Unix timestamp, 초)

    @JsonProperty("iat")
    private Long iat;               // 발급 시간 (Unix timestamp, 초)

    @JsonProperty("sub")
    private String sub;             // 사용자 고유 ID (providerId)

    @JsonProperty("email")
    private String email;           // 사용자 이메일

    @JsonProperty("email_verified")
    private Boolean emailVerified;  // 이메일 인증 여부 ("true" string 또는 boolean)

    @JsonProperty("is_private_email")
    private Boolean isPrivateEmail; // 애플 프라이빗 이메일 사용 여부
}