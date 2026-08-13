package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import com.devkor.ifive.nadab.domain.stats.core.repository.PeakStatsRepository;
import com.devkor.ifive.nadab.domain.stats.core.repository.PeakStatsStore;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PeakStatsInitializerTest {

    private final PeakStatsRepository repository = mock(PeakStatsRepository.class);
    private final PeakStatsStore store = mock(PeakStatsStore.class);
    private final PeakStatsInitializer initializer = new PeakStatsInitializer(repository, store);

    @Test
    void run_initializes_all_aggregated_peak_statistics() {
        PeakStat dau = new PeakStat(10L, LocalDate.of(2026, 8, 1));
        PeakStat wau = new PeakStat(20L, LocalDate.of(2026, 7, 27));
        when(repository.findAllPeakStats()).thenReturn(Map.of(
                PeakMetric.DAU, dau,
                PeakMetric.WAU, wau
        ));

        initializer.run(null);

        verify(store).updateIfGreater(PeakMetric.DAU, dau);
        verify(store).updateIfGreater(PeakMetric.WAU, wau);
    }

    @Test
    void run_does_not_fail_application_when_initial_aggregation_fails() {
        when(repository.findAllPeakStats()).thenThrow(new IllegalStateException("database unavailable"));

        assertThatCode(() -> initializer.run(null)).doesNotThrowAnyException();

        verifyNoInteractions(store);
    }
}
