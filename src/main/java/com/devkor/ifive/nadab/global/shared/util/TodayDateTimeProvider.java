package com.devkor.ifive.nadab.global.shared.util;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 오늘 날짜를 제공하는 유틸리티 클래스
 * repository 등에서 오늘 날짜 범위 조회 시 사용
 */
public class TodayDateTimeProvider {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private TodayDateTimeProvider() {
    }

    public static LocalDate getTodayDate() {
        return LocalDate.now(SEOUL);
    }
}
