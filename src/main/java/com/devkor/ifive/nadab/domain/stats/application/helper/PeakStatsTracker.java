package com.devkor.ifive.nadab.domain.stats.application.helper;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.PeakStatsStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PeakStatsTracker {

    private final PeakStatsStore peakStatsStore;

    public PeakStatViewModel updateAndGet(
            PeakMetric metric,
            List<LocalDate> periodStarts,
            List<Long> values,
            LocalDate currentPeriodStart
    ) {
        if (periodStarts.size() != values.size()) {
            throw new IllegalArgumentException("periodStarts and values must have the same size");
        }

        PeakStat candidate = findPeakCandidate(periodStarts, values);
        if (candidate != null) {
            peakStatsStore.updateIfGreater(metric, candidate);
        }

        return peakStatsStore.find(metric)
                .map(peak -> PeakStatViewModel.from(peak, metric.periodUnit(), currentPeriodStart))
                .orElseGet(PeakStatViewModel::empty);
    }

    private PeakStat findPeakCandidate(List<LocalDate> periodStarts, List<Long> values) {
        PeakStat candidate = null;

        for (int i = 0; i < periodStarts.size(); i++) {
            long value = values.get(i);
            LocalDate periodStart = periodStarts.get(i);

            if (value <= 0) {
                continue;
            }
            if (candidate == null
                    || value > candidate.value()
                    || value == candidate.value() && periodStart.isAfter(candidate.periodStart())) {
                candidate = new PeakStat(value, periodStart);
            }
        }

        return candidate;
    }
}
