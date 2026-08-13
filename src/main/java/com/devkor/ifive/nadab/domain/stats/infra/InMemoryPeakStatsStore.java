package com.devkor.ifive.nadab.domain.stats.infra;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import com.devkor.ifive.nadab.domain.stats.core.repository.PeakStatsStore;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;

@Component
public class InMemoryPeakStatsStore implements PeakStatsStore {

    private final ConcurrentMap<PeakMetric, PeakStat> peaks = new ConcurrentHashMap<>();

    @Override
    public Optional<PeakStat> find(PeakMetric metric) {
        return Optional.ofNullable(peaks.get(requireNonNull(metric, "metric must not be null")));
    }

    @Override
    public void updateIfGreater(PeakMetric metric, PeakStat candidate) {
        requireNonNull(metric, "metric must not be null");
        requireNonNull(candidate, "candidate must not be null");

        if (candidate.value() == 0) {
            return;
        }

        peaks.compute(metric, (key, current) -> shouldReplace(current, candidate) ? candidate : current);
    }

    private boolean shouldReplace(PeakStat current, PeakStat candidate) {
        if (current == null || candidate.value() > current.value()) {
            return true;
        }
        return candidate.value() == current.value()
                && candidate.periodStart().isAfter(current.periodStart());
    }
}
