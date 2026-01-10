package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import java.time.LocalDate;

public record MonthlyWeeklySummaryInputDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String discovered,
        String good,
        String improve
) {
}
