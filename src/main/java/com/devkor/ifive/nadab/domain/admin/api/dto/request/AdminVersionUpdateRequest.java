package com.devkor.ifive.nadab.domain.admin.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminVersionUpdateRequest(
        @NotBlank(message = "Version is required.")
        @Size(max = 30, message = "Version must be 30 characters or less.")
        @Pattern(regexp = "\\d+\\.\\d+\\.\\d+", message = "Version must use major.minor.patch format.")
        String version
) {
}
