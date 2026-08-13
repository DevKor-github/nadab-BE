package com.devkor.ifive.nadab.domain.stats.application.helper;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;
import com.devkor.ifive.nadab.domain.stats.infra.InMemoryPeakStatsStore;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeakStatsTrackerTest {

    private final InMemoryPeakStatsStore store = new InMemoryPeakStatsStore();
    private final PeakStatsTracker tracker = new PeakStatsTracker(store);

    @Test
    void updateAndGet_updates_peak_with_latest_period_when_values_are_equal() {
        LocalDate currentPeriodStart = LocalDate.of(2026, 8, 3);

        PeakStatViewModel peak = tracker.updateAndGet(
                PeakMetric.DAU,
                List.of(currentPeriodStart.minusDays(2), currentPeriodStart.minusDays(1), currentPeriodStart),
                List.of(4L, 9L, 9L),
                currentPeriodStart
        );

        assertThat(store.find(PeakMetric.DAU))
                .contains(new PeakStat(9L, currentPeriodStart));
        assertThat(peak).isEqualTo(new PeakStatViewModel(
                true, 9L, "2026-08-03", "2026-08-03", true
        ));
    }

    @Test
    void updateAndGet_keeps_greater_historical_peak() {
        LocalDate historicalPeriod = LocalDate.of(2026, 7, 1);
        store.updateIfGreater(PeakMetric.DAU, new PeakStat(20L, historicalPeriod));

        PeakStatViewModel peak = tracker.updateAndGet(
                PeakMetric.DAU,
                List.of(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)),
                List.of(8L, 10L),
                LocalDate.of(2026, 8, 2)
        );

        assertThat(peak.value()).isEqualTo(20L);
        assertThat(peak.periodStart()).isEqualTo("2026-07-01");
        assertThat(peak.currentPeriod()).isFalse();
    }

    @Test
    void updateAndGet_formats_week_and_month_periods() {
        PeakStatViewModel weeklyPeak = tracker.updateAndGet(
                PeakMetric.WAU,
                List.of(LocalDate.of(2026, 8, 10)),
                List.of(10L),
                LocalDate.of(2026, 8, 10)
        );
        PeakStatViewModel monthlyPeak = tracker.updateAndGet(
                PeakMetric.MAU,
                List.of(LocalDate.of(2026, 8, 1)),
                List.of(20L),
                LocalDate.of(2026, 8, 1)
        );

        assertThat(weeklyPeak.periodLabel()).isEqualTo("2026-08-10 ~ 2026-08-16");
        assertThat(monthlyPeak.periodLabel()).isEqualTo("2026-08");
    }

    @Test
    void updateAndGet_returns_empty_view_model_when_all_values_are_zero() {
        PeakStatViewModel peak = tracker.updateAndGet(
                PeakMetric.DAILY_SIGNUP,
                List.of(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)),
                List.of(0L, 0L),
                LocalDate.of(2026, 8, 2)
        );

        assertThat(peak).isEqualTo(PeakStatViewModel.empty());
        assertThat(store.find(PeakMetric.DAILY_SIGNUP)).isEmpty();
    }

    @Test
    void updateAndGet_rejects_different_period_and_value_sizes() {
        assertThatThrownBy(() -> tracker.updateAndGet(
                PeakMetric.DAU,
                List.of(LocalDate.of(2026, 8, 1)),
                List.of(1L, 2L),
                LocalDate.of(2026, 8, 1)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
