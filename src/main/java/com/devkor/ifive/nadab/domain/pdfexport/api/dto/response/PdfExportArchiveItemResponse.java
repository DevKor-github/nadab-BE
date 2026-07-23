package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 아카이브(이력) 목록 항목. 완료된 작업을 최신순으로 보여준다(다운로드는 항목별 발급 API로).
 */
public record PdfExportArchiveItemResponse(
        @Schema(description = "작업 id", example = "1")
        Long jobId,

        @Schema(description = "내보내기 유형 (REPORT_ONLY/ANSWER_ONLY/REPORT_AND_ANSWER)", example = "REPORT_AND_ANSWER")
        String type,

        @Schema(description = "기간 시작일", example = "2025-11-01")
        LocalDate startDate,

        @Schema(description = "기간 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "작업 상태 (아카이브는 COMPLETED만)", example = "COMPLETED")
        String status,

        @Schema(description = "다운로드 보관 만료 시각(완료 시각 + 7일)",
                example = "2025-12-07T05:30:00Z")
        OffsetDateTime expiresAt,

        @Schema(description = "COMPLETED 이후 보관 기간(7일)이 지나 만료됐는지 여부(만료 시 재생성 필요)", example = "false")
        boolean expired
) {
}