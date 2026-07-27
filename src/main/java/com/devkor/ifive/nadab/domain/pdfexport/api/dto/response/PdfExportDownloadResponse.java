package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * 다운로드 URL 발급 응답. 폴링(GET /{jobId})과 분리된 발급 전용 엔드포인트가 반환한다.
 */
public record PdfExportDownloadResponse(
        @Schema(description = "CloudFront signed 다운로드 URL (약 3분간 유효, 만료 시 재발급)")
        String downloadUrl,

        @Schema(description = "저장에 사용할 파일명. 필요하면 다운로드 저장 시 이 값을 그대로 사용",
                example = "나답_20251101-20251130.pdf")
        String fileName,

        @Schema(description = "다운로드 보관 만료 시각(완료 시각 + 7일). 이 시각이 지나면 재발급 불가",
                example = "2025-11-08T05:30:00Z")
        OffsetDateTime expiresAt
) {
}