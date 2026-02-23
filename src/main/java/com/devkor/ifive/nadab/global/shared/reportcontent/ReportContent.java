package com.devkor.ifive.nadab.global.shared.reportcontent;

public record ReportContent(
        StyledText discovered,
        StyledText improve
) {
    public ReportContent normalized() {
        StyledText d = discovered == null ? new StyledText(java.util.List.of()) : discovered.normalized();
        StyledText i = improve == null ? new StyledText(java.util.List.of()) : improve.normalized();
        return new ReportContent(d, i);
    }
}
