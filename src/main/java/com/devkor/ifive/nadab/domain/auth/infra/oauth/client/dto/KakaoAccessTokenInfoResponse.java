package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 Access Token 정보 조회 API 응답
 * - 카카오 토큰 정보 보기 API (/v1/user/access_token_info) 응답 형식
 * - Access Token의 유효성 및 앱 ID 검증에 사용
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAccessTokenInfoResponse {

    @JsonProperty("app_id")
    private Integer appId;  // 토큰이 발급된 앱 ID

    @JsonProperty("msg")
    private String msg;  // 에러 메시지

    @JsonProperty("code")
    private String code;  // 에러 코드
}