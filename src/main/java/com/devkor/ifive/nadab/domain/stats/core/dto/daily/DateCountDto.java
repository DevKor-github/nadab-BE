package com.devkor.ifive.nadab.domain.stats.core.dto.daily;

import java.time.LocalDate;

public record DateCountDto(
        LocalDate date,
        long count
) {}
