package com.devkor.ifive.nadab.domain.admin.api.dto.request;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminVersionCreateRequest(
        @NotNull(message = "플랫폼은 필수입니다.")
        AppPlatform platform,

        @NotBlank(message = "버전은 필수입니다.")
        @Size(max = 30, message = "버전은 30자 이하여야 합니다.")
        String version,

        @NotNull(message = "요약은 null일 수 없습니다.")
        @Size(max = 120, message = "요약은 120자 이하여야 합니다.")
        String summary
) {
}
