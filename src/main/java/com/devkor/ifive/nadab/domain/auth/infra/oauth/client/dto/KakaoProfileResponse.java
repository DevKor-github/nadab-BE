package com.devkor.ifive.nadab.domain.auth.infra.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 사용자 정보 API 응답
 * - 카카오 사용자 정보 요청 API (/v2/user/me) 응답 형식
 * - 성공: id, kakao_account 필드 포함
 * - 실패: msg, code 필드 포함
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfileResponse {

    @JsonProperty("id")
    private Long id;  // providerId

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("msg")
    private String msg;  // 에러 메시지

    @JsonProperty("code")
    private String code;  // 에러 코드

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;  // 이메일 제공 동의 필요 여부

        @JsonProperty("email")
        private String email;

        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;  // 이메일 유효성

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;  // 이메일 인증 여부
    }
}