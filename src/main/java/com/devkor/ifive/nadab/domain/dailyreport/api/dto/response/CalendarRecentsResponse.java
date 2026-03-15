package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "캘린더 최근 답변 미리보기 응답")
public record CalendarRecentsResponse(
        @Schema(description = "최근 답변 목록 (최대 6개, 날짜 내림차순)")
        List<AnswerEntrySummaryResponse> items
) {
    public static CalendarRecentsResponse from(List<AnswerEntrySummaryResponse> items) {
        return new CalendarRecentsResponse(items);
    }
}
