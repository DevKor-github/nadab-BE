package com.devkor.ifive.nadab.domain.stats.core.dto.peak;

import java.time.LocalDate;
import java.util.Objects;

public record PeakStat(
        long value,
        LocalDate periodStart
) {

    public PeakStat {
        if (value < 0) {
            throw new IllegalArgumentException("Peak value must not be negative");
        }
        Objects.requireNonNull(periodStart, "periodStart must not be null");
    }
}
