package com.devkor.ifive.nadab.global.shared.util;

import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

public final class MonthRangeCalculator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private MonthRangeCalculator() {
    }

    /**
     * 주어진 날짜가 속한 월(1일 ~ 말일) 범위를 반환합니다.
     */
    public static MonthRangeDto monthRangeOf(LocalDate date) {
        YearMonth ym = YearMonth.from(date);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return new MonthRangeDto(start, end);
    }

    /**
     * KST 기준 "저번 달" 범위를 반환합니다.
     */
    public static MonthRangeDto getLastMonthRange() {
        LocalDate today = LocalDate.now(KST);
        LocalDate lastMonthDate = today.minusMonths(1);
        return monthRangeOf(lastMonthDate);
    }

    /**
     * KST 기준 "이번 달" 범위를 반환합니다.
     */
    public static MonthRangeDto getThisMonthRange() {
        LocalDate today = LocalDate.now(KST);
        return monthRangeOf(today);
    }
}
