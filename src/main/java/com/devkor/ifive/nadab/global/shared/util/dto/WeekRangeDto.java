package com.devkor.ifive.nadab.global.shared.util.dto;

import java.time.LocalDate;

public record WeekRangeDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate
) {
}
