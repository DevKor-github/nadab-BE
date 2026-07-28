package com.devkor.ifive.nadab.domain.pdfexport.core.dto;

/**
 * reserve 커밋 후(AFTER_COMMIT) 백그라운드 렌더링을 트리거하는 이벤트.
 */
public record PdfExportRequestedEventDto(
        Long jobId,
        Long userId,
        Long crystalLogId
) {
}