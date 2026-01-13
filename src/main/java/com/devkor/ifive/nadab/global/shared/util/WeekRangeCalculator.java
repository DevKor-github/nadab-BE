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

    /**
     * 월요일 시작 ~ 일요일 종료 기준의 "2주 전" 범위를 반환합니다.
     */
    public static WeekRangeDto getTwoWeeksAgoRange() {
        LocalDate today = LocalDate.now(KST);
        LocalDate twoWeeksAgoDate = today.minusWeeks(2);
        return weekRangeOf(twoWeeksAgoDate);
    }

    /**
     * 주어진 WeekRange가 해당 월의 몇 주차인지 반환합니다.
     * (월요일 시작 기준, 해당 주의 월요일이 속한 달 기준)
     */
    public static int getWeekOfMonth(WeekRangeDto weekRange) {
        LocalDate weekStart = weekRange.weekStartDate();

        LocalDate firstMondayOfMonth =
                weekStart.withDayOfMonth(1)
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        return (int) java.time.temporal.ChronoUnit.WEEKS
                .between(firstMondayOfMonth, weekStart) + 1;
    }
}
