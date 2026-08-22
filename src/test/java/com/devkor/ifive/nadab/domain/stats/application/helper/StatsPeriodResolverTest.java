package com.devkor.ifive.nadab.domain.stats.application.helper;

import com.devkor.ifive.nadab.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatsPeriodResolverTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);

    @Test
    void resolves_missing_periods_to_current_day_week_and_month() {
        assertThat(StatsPeriodResolver.resolveDaily(null, TODAY)).isEqualTo(TODAY);
        assertThat(StatsPeriodResolver.resolveWeekly("", TODAY)).isEqualTo(LocalDate.of(2026, 8, 17));
        assertThat(StatsPeriodResolver.resolveMonthly("  ", TODAY)).isEqualTo(YearMonth.of(2026, 8));
    }

    @Test
    void parses_supported_period_formats_and_iso_week_year_boundary() {
        assertThat(StatsPeriodResolver.resolveDaily("2026-08-13", TODAY))
                .isEqualTo(LocalDate.of(2026, 8, 13));
        assertThat(StatsPeriodResolver.resolveWeekly("2025-W01", TODAY))
                .isEqualTo(LocalDate.of(2024, 12, 30));
        assertThat(StatsPeriodResolver.formatIsoWeek(LocalDate.of(2024, 12, 30)))
                .isEqualTo("2025-W01");
        assertThat(StatsPeriodResolver.resolveMonthly("2026-07", TODAY))
                .isEqualTo(YearMonth.of(2026, 7));
    }

    @Test
    void rejects_invalid_or_future_periods() {
        assertThatThrownBy(() -> StatsPeriodResolver.resolveDaily("2026-02-30", TODAY))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StatsPeriodResolver.resolveWeekly("2026-W99", TODAY))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StatsPeriodResolver.resolveMonthly("2026-13", TODAY))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StatsPeriodResolver.resolveDaily("2026-08-21", TODAY))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StatsPeriodResolver.resolveWeekly("2026-W35", TODAY))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StatsPeriodResolver.resolveMonthly("2026-09", TODAY))
                .isInstanceOf(BadRequestException.class);
    }
}
