package com.devkor.ifive.nadab.domain.overallreport.core.dto;

import java.time.LocalDate;
import java.util.List;

// 통계 정보
public record OverallStatsDto(
        List<CountItem> emotionTop3,
        List<CountItem> interestTop3,
        List<CountItem> questionLevelDist,
        double avgDaysPerWeek,
        double avgDaysPerMonth,
        int maxStreakGapDays,
        LocalDate firstRecordDate,
        LocalDate lastRecordDate,
        int totalRecords
) {
    public record CountItem(String key, long count) { }
}
