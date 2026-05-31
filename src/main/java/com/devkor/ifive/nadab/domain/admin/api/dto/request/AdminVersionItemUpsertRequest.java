package com.devkor.ifive.nadab.domain.admin.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminVersionItemUpsertRequest(
        @NotBlank(message = "업데이트명은 필수입니다.")
        @Size(max = 100, message = "업데이트명은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "상세 내용은 필수입니다.")
        @Size(max = 500, message = "상세 내용은 500자 이하여야 합니다.")
        String description,

        @NotNull(message = "displayOrder는 필수입니다.")
        @Min(value = 1, message = "displayOrder는 1 이상이어야 합니다.")
        @Max(value = 9999, message = "displayOrder는 9999 이하여야 합니다.")
        Integer displayOrder
) {
}
