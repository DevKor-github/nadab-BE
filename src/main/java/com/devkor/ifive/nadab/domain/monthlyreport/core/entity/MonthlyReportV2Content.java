package com.devkor.ifive.nadab.domain.monthlyreport.core.entity;

import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;

public record MonthlyReportV2Content(
        String summary,
        String commentSummary,
        String dominantKeyword,
        StyledText discovered,
        StyledText comment
) {
    public MonthlyReportV2Content normalized() {
        String s = (summary == null) ? "" : summary.trim();
        String cs = (commentSummary == null) ? "" : commentSummary.trim();
        String dk = (dominantKeyword == null) ? "" : dominantKeyword.trim();
        StyledText d = discovered == null ? new StyledText(java.util.List.of()) : discovered.normalized();
        StyledText c = comment == null ? new StyledText(java.util.List.of()) : comment.normalized();
        return new MonthlyReportV2Content(s, cs, dk, d, c);
    }
}
