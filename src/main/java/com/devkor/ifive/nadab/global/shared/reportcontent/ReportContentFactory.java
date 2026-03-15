package com.devkor.ifive.nadab.global.shared.reportcontent;

import java.util.List;

public final class ReportContentFactory {

    private ReportContentFactory() { }

    public static ReportContent empty() {
        // NOT NULL 만족용 최소 구조: 빈 문자열 1세그먼트
        StyledText oneEmpty = new StyledText(
                List.of(new Segment("", List.of()))
        );
        return new ReportContent("", oneEmpty, oneEmpty);
    }

    public static StyledText fromPlain(String text) {
        return new StyledText(List.of(new Segment(text == null ? "" : text, List.of())));
    }

    public static ReportContent fromPlain(String summary, String discovered, String improve) {
        return new ReportContent(summary == null ? "" : summary, fromPlain(discovered), fromPlain(improve));
    }
}
