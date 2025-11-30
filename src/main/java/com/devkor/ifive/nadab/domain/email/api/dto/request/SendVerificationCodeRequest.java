package com.devkor.ifive.nadab.domain.email.api.dto.request;

import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "이메일 인증 코드 발송 요청")
public record SendVerificationCodeRequest(
        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "인증 타입 (SIGNUP: 회원가입, PASSWORD_RESET: 비밀번호 재설정)", example = "SIGNUP")
        @NotNull(message = "인증 타입은 필수입니다")
        VerificationType verificationType
) {
}