package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 네이버 사용자 정보 API 응답
 * - 네이버 프로필 정보 조회 API (/v1/nid/me) 응답 형식
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProfileResponse {
    @JsonProperty("resultcode")
    private String resultcode;  // "00" = 성공

    @JsonProperty("message")
    private String message;

    @JsonProperty("response")
    private ProfileData response;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileData {
        @JsonProperty("id")
        private String id;       // providerId

        @JsonProperty("email")
        private String email;
    }
}