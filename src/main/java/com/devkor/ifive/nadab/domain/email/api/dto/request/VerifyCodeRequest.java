package com.devkor.ifive.nadab.domain.email.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 인증 코드 검증 요청")
public record VerifyCodeRequest(
        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "6자리 인증 코드", example = "123456")
        @NotBlank(message = "인증 코드는 필수입니다")
        @Pattern(regexp = "^[1-9][0-9]{5}$", message = "인증 코드는 6자리 숫자여야 합니다")
        String code,

        @Schema(description = "인증 타입 (SIGNUP: 회원가입, PASSWORD_RESET: 비밀번호 재설정)", example = "SIGNUP")
        @NotBlank(message = "인증 타입은 필수입니다")
        String verificationType
) {
}