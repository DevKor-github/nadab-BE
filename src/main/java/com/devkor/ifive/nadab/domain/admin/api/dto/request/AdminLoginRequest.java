package com.devkor.ifive.nadab.domain.admin.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
