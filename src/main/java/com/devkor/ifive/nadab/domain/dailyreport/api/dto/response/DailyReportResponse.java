package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 리포트 조회 응답")
public record DailyReportResponse(
        @Schema(description = "오늘의 리포트 내용")
        String content,

        @Schema(description = "오늘의 리포트 감정 상태", example = "GROWTH")
        String emotion
) {
}
