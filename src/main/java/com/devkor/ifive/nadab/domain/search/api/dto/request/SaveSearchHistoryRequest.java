package com.devkor.ifive.nadab.domain.search.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "검색어 저장 요청")
public record SaveSearchHistoryRequest(
        @Schema(description = "검색어", example = "행복했던 순간")
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하여야 합니다.")
        String keyword
) {
}