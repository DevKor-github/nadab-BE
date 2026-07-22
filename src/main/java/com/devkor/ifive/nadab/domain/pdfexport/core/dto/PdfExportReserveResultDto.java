package com.devkor.ifive.nadab.domain.pdfexport.core.dto;

/**
 * reserve(선차감 + 로그 + IN_PROGRESS 전환) 결과.
 */
public record PdfExportReserveResultDto(
        Long jobId,
        long balanceAfter
) {
}