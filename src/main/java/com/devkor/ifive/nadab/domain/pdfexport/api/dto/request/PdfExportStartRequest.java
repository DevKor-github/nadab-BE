package com.devkor.ifive.nadab.domain.pdfexport.api.dto.request;

import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PdfExportStartRequest(
        @Schema(description = "내보내기 유형", example = "REPORT_AND_ANSWER")
        @NotNull
        PdfExportType type,

        @Schema(description = "시작일 (포함)", example = "2026-01-01")
        @NotNull
        LocalDate startDate,

        @Schema(description = "종료일 (포함). 종료일 기준 최대 1년", example = "2026-06-30")
        @NotNull
        LocalDate endDate
) {
}