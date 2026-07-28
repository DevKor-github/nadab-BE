package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PdfExportStartResponse(
        @Schema(description = "생성된(또는 재사용된) 작업 id", example = "1")
        Long jobId,

        @Schema(description = "작업 상태", example = "PENDING")
        String status,

        @Schema(description = "차감 후 크리스탈 잔액. 멱등 재사용으로 재과금이 없으면 null", example = "150")
        Long balanceAfter
) {
}