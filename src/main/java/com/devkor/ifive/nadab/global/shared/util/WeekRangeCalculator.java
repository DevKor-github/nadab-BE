package com.devkor.ifive.nadab.global.shared.util;

import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public final class WeekRangeCalculator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private WeekRangeCalculator() {
    }

    /**
     * 주어진 날짜가 속한 주(월~일) 범위를 반환합니다.
     */
    public static WeekRangeDto weekRangeOf(LocalDate date) {
        LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return new WeekRangeDto(start, end);
    }

    /**
     * 월요일 시작 ~ 일요일 종료 기준의 "저번 주" 범위를 반환합니다.
     */
    public static WeekRangeDto getLastWeekRange() {
        LocalDate today = LocalDate.now(KST);
        LocalDate lastWeekDate = today.minusWeeks(1);
        return weekRangeOf(lastWeekDate);
    }

}
