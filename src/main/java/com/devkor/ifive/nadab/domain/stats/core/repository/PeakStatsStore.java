package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;

import java.util.Optional;

public interface PeakStatsStore {

    Optional<PeakStat> find(PeakMetric metric);

    /**
     * Stores a positive candidate when it is greater than the current record.
     * When values are equal, the more recent period is retained.
     */
    void updateIfGreater(PeakMetric metric, PeakStat candidate);
}
