package com.devkor.ifive.nadab.global.shared.util.dto;

import java.time.LocalDate;

public record MonthRangeDto(
        LocalDate monthStartDate,
        LocalDate monthEndDate
) {
}
