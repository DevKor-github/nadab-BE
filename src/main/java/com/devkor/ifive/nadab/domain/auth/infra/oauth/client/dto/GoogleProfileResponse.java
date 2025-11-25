package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구글 사용자 정보 API 응답
 * - 구글 UserInfo API v3 (/oauth2/v3/userinfo) 응답 형식
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleProfileResponse {
    @JsonProperty("sub")
    private String sub;      // providerId

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;  // 이메일 인증 여부

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}