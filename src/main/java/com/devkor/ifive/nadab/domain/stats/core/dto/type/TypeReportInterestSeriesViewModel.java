package com.devkor.ifive.nadab.domain.stats.core.dto.type;

import java.util.List;

public record TypeReportInterestSeriesViewModel(
        String interestCode,
        String interestLabel,
        List<Long> counts
) {}
