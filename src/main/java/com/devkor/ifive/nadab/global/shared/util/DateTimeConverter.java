package com.devkor.ifive.nadab.global.shared.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * OffsetDateTime을 서울 시간대의 LocalDate 또는 LocalDateTime으로 변환하는 유틸리티 클래스
 */
public class DateTimeConverter {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static LocalDate convertToSeoulDate(OffsetDateTime odt) {
        if (odt == null) return null;
        return odt.atZoneSameInstant(SEOUL).toLocalDate();
    }

    public static LocalDateTime convertToSeoulDateTime(OffsetDateTime odt) {
        if (odt == null) return null;
        return odt.atZoneSameInstant(SEOUL).toLocalDateTime();
    }
}
