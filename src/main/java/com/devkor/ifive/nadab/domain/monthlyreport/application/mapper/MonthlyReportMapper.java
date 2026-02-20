package com.devkor.ifive.nadab.domain.monthlyreport.application.mapper;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;

public final class MonthlyReportMapper {
    private MonthlyReportMapper() {}

    public static MonthlyReportResponse toResponse(MonthRangeDto range, MonthlyReport report) {
        return new MonthlyReportResponse(
                range.monthStartDate().getMonthValue(),
                report.getDiscovered(),
                report.getImprove(),
                report.getStatus().name()
        );
    }
}

