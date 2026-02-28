package com.devkor.ifive.nadab.global.shared.reportcontent;

public record ReportContent(
        String summary,
        StyledText discovered,
        StyledText improve
) {
    public ReportContent normalized() {
        String s = (summary == null) ? "" : summary.trim();
        StyledText d = discovered == null ? new StyledText(java.util.List.of()) : discovered.normalized();
        StyledText i = improve == null ? new StyledText(java.util.List.of()) : improve.normalized();
        return new ReportContent(s, d, i);
    }
}
