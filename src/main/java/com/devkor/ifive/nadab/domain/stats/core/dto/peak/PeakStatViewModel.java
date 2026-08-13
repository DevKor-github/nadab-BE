package com.devkor.ifive.nadab.domain.stats.core.dto.peak;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record PeakStatViewModel(
        boolean available,
        long value,
        String periodStart,
        String periodLabel,
        boolean currentPeriod
) {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static PeakStatViewModel from(
            PeakStat peak,
            PeakPeriodUnit periodUnit,
            LocalDate currentPeriodStart
    ) {
        return new PeakStatViewModel(
                true,
                peak.value(),
                peak.periodStart().toString(),
                formatPeriodLabel(peak.periodStart(), periodUnit),
                peak.periodStart().equals(currentPeriodStart)
        );
    }

    public static PeakStatViewModel empty() {
        return new PeakStatViewModel(false, 0L, "", "", false);
    }

    private static String formatPeriodLabel(LocalDate periodStart, PeakPeriodUnit periodUnit) {
        return switch (periodUnit) {
            case DAY -> periodStart.toString();
            case WEEK -> periodStart + " ~ " + periodStart.plusDays(6);
            case MONTH -> MONTH_FORMATTER.format(periodStart);
        };
    }
}
