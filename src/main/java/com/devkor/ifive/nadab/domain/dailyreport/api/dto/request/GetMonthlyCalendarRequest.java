package com.devkor.ifive.nadab.domain.dailyreport.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "월별 캘린더 조회 요청")
public record GetMonthlyCalendarRequest(
        @Schema(description = "연도", example = "2026")
        @NotNull(message = "연도는 필수입니다")
        Integer year,

        @Schema(description = "월 (1~12)", example = "1")
        @NotNull(message = "월은 필수입니다")
        @Min(value = 1, message = "월은 1~12 사이의 값이어야 합니다")
        @Max(value = 12, message = "월은 1~12 사이의 값이어야 합니다")
        Integer month
) {
}
