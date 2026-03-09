package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.MonthlyCalendarDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "캘린더 날짜별 정보")
public record CalendarEntryResponse(
        @Schema(description = "답변 날짜", example = "2026-01-30")
        LocalDate date,

        @Schema(description = "감정 코드 (답변이 있고 리포트가 COMPLETED 상태일 때만 제공)", example = "ACHIEVEMENT")
        String emotionCode
) {
    public static CalendarEntryResponse from(MonthlyCalendarDto dto) {
        return new CalendarEntryResponse(
                dto.date(),
                dto.emotionCode() != null ? dto.emotionCode().name() : null
        );
    }
}
