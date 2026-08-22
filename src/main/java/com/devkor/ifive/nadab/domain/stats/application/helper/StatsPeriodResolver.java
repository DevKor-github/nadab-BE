package com.devkor.ifive.nadab.domain.stats.application.helper;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public final class StatsPeriodResolver {

    private static final DateTimeFormatter ISO_WEEK_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(IsoFields.WEEK_BASED_YEAR, 4)
            .appendLiteral("-W")
            .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
            .toFormatter(Locale.ROOT);

    private StatsPeriodResolver() {}

    public static LocalDate resolveDaily(String value) {
        return resolveDaily(value, TodayDateTimeProvider.getTodayDate());
    }

    public static LocalDate resolveWeekly(String value) {
        return resolveWeekly(value, TodayDateTimeProvider.getTodayDate());
    }

    public static YearMonth resolveMonthly(String value) {
        return resolveMonthly(value, TodayDateTimeProvider.getTodayDate());
    }

    public static String formatIsoWeek(LocalDate weekStart) {
        return ISO_WEEK_FORMATTER.format(
                weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        );
    }

    static LocalDate resolveDaily(String value, LocalDate today) {
        LocalDate selectedDate = isBlank(value) ? today : parseDate(value);
        if (selectedDate.isAfter(today)) {
            throw invalidPeriod();
        }
        return selectedDate;
    }

    static LocalDate resolveWeekly(String value, LocalDate today) {
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate selectedWeekStart = isBlank(value) ? currentWeekStart : parseWeekStart(value);
        if (selectedWeekStart.isAfter(currentWeekStart)) {
            throw invalidPeriod();
        }
        return selectedWeekStart;
    }

    static YearMonth resolveMonthly(String value, LocalDate today) {
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth selectedMonth = isBlank(value) ? currentMonth : parseMonth(value);
        if (selectedMonth.isAfter(currentMonth)) {
            throw invalidPeriod();
        }
        return selectedMonth;
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw invalidPeriod();
        }
    }

    private static LocalDate parseWeekStart(String value) {
        try {
            return LocalDate.parse(value + "-1", DateTimeFormatter.ISO_WEEK_DATE);
        } catch (DateTimeParseException e) {
            throw invalidPeriod();
        }
    }

    private static YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throw invalidPeriod();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static BadRequestException invalidPeriod() {
        return new BadRequestException(ErrorCode.VALIDATION_FAILED);
    }
}
