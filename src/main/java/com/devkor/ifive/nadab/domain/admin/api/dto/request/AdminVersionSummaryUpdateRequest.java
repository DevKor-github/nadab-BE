package com.devkor.ifive.nadab.domain.admin.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminVersionSummaryUpdateRequest(
        @NotNull(message = "요약은 null일 수 없습니다.")
        @Size(max = 120, message = "요약은 120자 이하여야 합니다.")
        String summary
) {
}
