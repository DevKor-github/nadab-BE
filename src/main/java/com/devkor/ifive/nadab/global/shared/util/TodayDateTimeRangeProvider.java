package com.devkor.ifive.nadab.global.shared.util;

import com.devkor.ifive.nadab.global.shared.util.dto.TodayDateTimeRangeDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * 오늘 날짜의 시작과 내일 날짜의 시작을 제공하는 유틸리티 클래스
 * repository 등에서 오늘 날짜 범위 조회 시 사용
 */
public class TodayDateTimeRangeProvider {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private TodayDateTimeRangeProvider() {
    }

    public static TodayDateTimeRangeDto get() {
        LocalDate today = LocalDate.now(SEOUL);

        OffsetDateTime startOfToday =
                today.atStartOfDay(SEOUL).toOffsetDateTime();

        OffsetDateTime startOfTomorrow =
                today.plusDays(1)
                        .atStartOfDay(SEOUL)
                        .toOffsetDateTime();

        return new TodayDateTimeRangeDto(startOfToday, startOfTomorrow);
    }
}
