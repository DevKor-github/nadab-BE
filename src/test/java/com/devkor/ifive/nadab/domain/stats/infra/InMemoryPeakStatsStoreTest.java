package com.devkor.ifive.nadab.domain.stats.infra;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryPeakStatsStoreTest {

    private final InMemoryPeakStatsStore store = new InMemoryPeakStatsStore();

    @Test
    void updateIfGreater_stores_first_positive_record() {
        PeakStat peak = new PeakStat(10L, LocalDate.of(2026, 8, 1));

        store.updateIfGreater(PeakMetric.DAU, peak);

        assertThat(store.find(PeakMetric.DAU)).contains(peak);
    }

    @Test
    void updateIfGreater_replaces_record_with_greater_value() {
        store.updateIfGreater(PeakMetric.DAU, new PeakStat(10L, LocalDate.of(2026, 8, 1)));
        PeakStat greater = new PeakStat(11L, LocalDate.of(2026, 8, 2));

        store.updateIfGreater(PeakMetric.DAU, greater);

        assertThat(store.find(PeakMetric.DAU)).contains(greater);
    }

    @Test
    void updateIfGreater_keeps_record_when_candidate_is_smaller() {
        PeakStat current = new PeakStat(10L, LocalDate.of(2026, 8, 1));
        store.updateIfGreater(PeakMetric.DAU, current);

        store.updateIfGreater(PeakMetric.DAU, new PeakStat(9L, LocalDate.of(2026, 8, 2)));

        assertThat(store.find(PeakMetric.DAU)).contains(current);
    }

    @Test
    void updateIfGreater_uses_latest_period_when_values_are_equal() {
        store.updateIfGreater(PeakMetric.DAU, new PeakStat(10L, LocalDate.of(2026, 8, 1)));
        PeakStat latest = new PeakStat(10L, LocalDate.of(2026, 8, 2));

        store.updateIfGreater(PeakMetric.DAU, latest);

        assertThat(store.find(PeakMetric.DAU)).contains(latest);
    }

    @Test
    void updateIfGreater_keeps_latest_period_when_equal_candidate_is_older() {
        PeakStat latest = new PeakStat(10L, LocalDate.of(2026, 8, 2));
        store.updateIfGreater(PeakMetric.DAU, latest);

        store.updateIfGreater(PeakMetric.DAU, new PeakStat(10L, LocalDate.of(2026, 8, 1)));

        assertThat(store.find(PeakMetric.DAU)).contains(latest);
    }

    @Test
    void updateIfGreater_ignores_zero_value() {
        store.updateIfGreater(PeakMetric.DAU, new PeakStat(0L, LocalDate.of(2026, 8, 1)));

        assertThat(store.find(PeakMetric.DAU)).isEmpty();
    }

    @Test
    void updateIfGreater_keeps_greatest_value_during_concurrent_updates()
            throws InterruptedException, ExecutionException {
        int candidateCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Callable<Void>> updates = new ArrayList<>();
        for (int value = 1; value <= candidateCount; value++) {
            int candidateValue = value;
            updates.add(() -> {
                store.updateIfGreater(
                        PeakMetric.DAU,
                        new PeakStat(candidateValue, LocalDate.of(2026, 8, 1).plusDays(candidateValue))
                );
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executor.invokeAll(updates);
            for (Future<Void> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(store.find(PeakMetric.DAU))
                .contains(new PeakStat(candidateCount, LocalDate.of(2026, 8, 1).plusDays(candidateCount)));
    }

    @Test
    void peakStat_rejects_negative_value() {
        assertThatThrownBy(() -> new PeakStat(-1L, LocalDate.of(2026, 8, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
