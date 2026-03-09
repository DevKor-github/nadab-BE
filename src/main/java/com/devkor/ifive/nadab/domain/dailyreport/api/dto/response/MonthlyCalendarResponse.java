package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "월별 캘린더 응답")
public record MonthlyCalendarResponse(
        @Schema(description = "답변이 있는 날짜의 정보 목록 (답변 없는 날짜는 포함되지 않음)")
        List<CalendarEntryResponse> calendarEntries
) {
}
