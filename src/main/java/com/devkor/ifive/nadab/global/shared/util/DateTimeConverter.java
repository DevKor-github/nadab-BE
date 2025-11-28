package com.devkor.ifive.nadab.global.shared.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

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
