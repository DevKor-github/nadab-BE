package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestoreRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8자 이상)", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        @Pattern(regexp = ".*[A-Za-z].*", message = "비밀번호에는 영문자가 포함되어야 합니다")
        @Pattern(regexp = ".*\\d.*", message = "비밀번호에는 숫자가 포함되어야 합니다")
        @Pattern(regexp = ".*[^0-9A-Za-z].*", message = "비밀번호에는 특수문자(또는 공백)가 포함되어야 합니다")
        String password
) {}