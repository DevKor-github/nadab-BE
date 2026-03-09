package com.devkor.ifive.nadab.domain.weeklyreport.application.mapper;

import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;

public final class WeeklyReportMapper {
    private WeeklyReportMapper() {}

    public static WeeklyReportResponse toResponse(WeekRangeDto range, WeeklyReport report) {
        return new WeeklyReportResponse(
                range.weekStartDate().getMonthValue(),
                WeekRangeCalculator.getWeekOfMonth(range),
                report.getSummary(),
                report.getDiscovered(),
                report.getImprove(),
                report.getContent(),
                report.getStatus().name()
        );
    }
}

