package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 (마이페이지)")
public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "currentPassword123!")
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        String currentPassword,

        @Schema(description = "새로운 비밀번호 (영문, 숫자, 특수문자 포함 8자 이상)", example = "newPassword123!")
        @NotBlank(message = "새로운 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        @Pattern(regexp = ".*[A-Za-z].*", message = "비밀번호에는 영문자가 포함되어야 합니다")
        @Pattern(regexp = ".*\\d.*", message = "비밀번호에는 숫자가 포함되어야 합니다")
        @Pattern(regexp = ".*[^0-9A-Za-z].*", message = "비밀번호에는 특수문자(또는 공백)가 포함되어야 합니다")
        String newPassword
) {
}