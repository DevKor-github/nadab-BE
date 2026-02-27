package com.devkor.ifive.nadab.domain.stats.core.dto;

import java.time.LocalDate;

public record DateCountDto(
        LocalDate date,
        long count
) {}
