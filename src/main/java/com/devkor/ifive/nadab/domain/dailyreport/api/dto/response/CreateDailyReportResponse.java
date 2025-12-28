package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 리포트 생성 응답")
public record CreateDailyReportResponse(
        @Schema(description = "오늘의 리포트 ID", example = "1")
        Long reportId,

        @Schema(description = "오늘의 리포트 내용")
        String content,

        @Schema(description = "오늘의 리포트 감정 상태", example = "GROWTH")
        String emotion,

        @Schema(description = "리포트 작성 후 크리스탈 잔액", example = "100")
        Long balanceAfter
) {
}
