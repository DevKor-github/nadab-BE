package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;

public record AiMonthlyReportResultDto(
        MonthlyReportV2Content content,
        TypeTextContent emotionSummaryContent
) {
}
