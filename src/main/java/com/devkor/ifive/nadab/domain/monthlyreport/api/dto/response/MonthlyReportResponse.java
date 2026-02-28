package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월간 리포트 조회 응답")
public record MonthlyReportResponse(

        @Schema(description = "리포트가 작성된 달")
        int month,

        @Schema(description = "리포트 요약")
        String summary,

        @Schema(description = "이런 면도 발견되었어요")
        String discovered,

        @Schema(description = "다음엔 이렇게 보완해볼까요?")
        String improve,

        @Schema(description = "styled content (discovered/improve segments)")
        ReportContent content,

        @Schema(description = "상태", example = "PENDING")
        String status
) {
}
