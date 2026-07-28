package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record PdfExportStatusResponse(
        @Schema(description = "작업 id", example = "1")
        Long jobId,

        @Schema(description = "작업 상태 (PENDING/IN_PROGRESS/COMPLETED/FAILED)", example = "COMPLETED")
        String status,

        @Schema(description = "COMPLETED 시 다운로드 보관 만료 시각(완료 시각 + 7일). 그 외 null",
                example = "2025-11-08T05:30:00Z")
        OffsetDateTime expiresAt,

        @Schema(description = "COMPLETED 이후 보관 기간(7일)이 지나 만료됐는지 여부. 만료되면 다운로드 발급 불가(재생성 필요)",
                example = "false")
        boolean expired,

        @Schema(description = "FAILED 시 실패 코드 (그 외 null)")
        String errorCode
) {
}