package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구글 ID Token 검증 API 응답
 * - 구글 tokeninfo API (/tokeninfo?id_token=...) 응답 형식
 * - 성공: iss, sub, aud, email, exp 등 포함
 * - 실패: error, error_description 포함
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleIdTokenInfoResponse {

    @JsonProperty("iss")
    private String iss;      // 토큰 발급자 (https://accounts.google.com)

    @JsonProperty("sub")
    private String sub;      // 사용자 고유 ID (providerId)

    @JsonProperty("azp")
    private String azp;      // Authorized party (클라이언트 ID)

    @JsonProperty("aud")
    private String aud;      // Audience (우리 앱의 Client ID)

    @JsonProperty("email")
    private String email;    // 사용자 이메일

    @JsonProperty("email_verified")
    private Boolean emailVerified;  // 이메일 인증 여부

    @JsonProperty("exp")
    private Long exp;        // 만료 시간 (Unix timestamp, 초)

    @JsonProperty("iat")
    private Long iat;        // 발급 시간 (Unix timestamp, 초)

    @JsonProperty("error")
    private String error;    // 에러 코드 (실패 시)

    @JsonProperty("error_description")
    private String errorDescription;  // 에러 설명 (실패 시)
}