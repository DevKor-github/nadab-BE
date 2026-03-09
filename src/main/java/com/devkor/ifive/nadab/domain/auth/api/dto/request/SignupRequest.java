package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 */
@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "이메일 (이메일 인증 완료 필수)", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8자 이상)", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        @Pattern(regexp = ".*[A-Za-z].*", message = "비밀번호에는 영문자가 포함되어야 합니다")
        @Pattern(regexp = ".*\\d.*", message = "비밀번호에는 숫자가 포함되어야 합니다")
        @Pattern(regexp = ".*[^0-9A-Za-z].*", message = "비밀번호에는 특수문자(또는 공백)가 포함되어야 합니다")
        String password,

        @Schema(description = "서비스 이용약관 동의", example = "true")
        @NotNull(message = "서비스 이용약관 동의는 필수입니다")
        Boolean service,

        @Schema(description = "개인정보 처리방침 동의", example = "true")
        @NotNull(message = "개인정보 처리방침 동의는 필수입니다")
        Boolean privacy,

        @Schema(description = "만 14세 이상 확인", example = "true")
        @NotNull(message = "만 14세 이상 확인은 필수입니다")
        Boolean ageVerification,

        @Schema(description = "마케팅 정보 수신 동의 (선택)", example = "false")
        @NotNull(message = "마케팅 동의 여부는 필수입니다")
        Boolean marketing
) {
}