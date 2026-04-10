package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 애플 Access Token 응답 DTO
 * - 성공: access_token, refresh_token, id_token
 * - 실패: error, error_description
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;    // KMS 암호화해서 저장

    @JsonProperty("id_token")
    private String idToken;         // JWT 형식, 사용자 정보 포함

    @JsonProperty("token_type")
    private String tokenType;       // "Bearer"

    @JsonProperty("expires_in")
    private Long expiresIn;         // 초 단위

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}