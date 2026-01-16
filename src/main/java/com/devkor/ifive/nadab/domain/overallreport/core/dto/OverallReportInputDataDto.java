package com.devkor.ifive.nadab.domain.overallreport.core.dto;

// 최종 입력
public record OverallReportInputDataDto(
        String monthlySummaries,
        String weeklySummaries,
        String representativeEntries,
        String statsSummary
) {
}
