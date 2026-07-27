package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 동시 생성 1개 제한에 걸려 거부(409)될 때, 이미 생성 중인 작업의 id.
 * 클라는 이 jobId로 그 생성 화면(로딩 화면)으로 이동하고, 상세는 GET /pdf-exports/current로 받는다.
 */
public record PdfExportInProgressResponse(
        @Schema(description = "이미 생성 중인 작업 id", example = "1")
        Long jobId
) {
}