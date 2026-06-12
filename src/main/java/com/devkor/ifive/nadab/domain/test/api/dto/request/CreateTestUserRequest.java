package com.devkor.ifive.nadab.domain.test.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTestUserRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email format is invalid")
        String email,

        @NotBlank(message = "nickname is required")
        @Size(min = 2, max = 10, message = "nickname must be between 2 and 10 characters")
        String nickname
) {
}
