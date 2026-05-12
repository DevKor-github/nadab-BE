package com.devkor.ifive.nadab.domain.monthlyreport.core.entity;

import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;

public record MonthlyReportV2Content(
        String summary,
        String commentSummary,
        String dominantKeyword,
        StyledText discovered,
        StyledText comment
) {
}
