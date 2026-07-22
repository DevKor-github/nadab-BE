package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 생성/차감 전 미리보기: 해당 기간에 포함될 답변·주간·월간 개수. 유형과 무관하게 3종 모두 내려준다
 */
public record PdfExportPreviewResponse(
        @Schema(description = "기간 내 답변 수", example = "30")
        long answerCount,

        @Schema(description = "기간과 겹치는 완료 주간 리포트 수", example = "4")
        long weeklyCount,

        @Schema(description = "기간과 겹치는 완료 월간 리포트 수", example = "1")
        long monthlyCount
) {
}