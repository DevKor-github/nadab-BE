package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;

import java.util.List;

public final class MonthlyReportV2ContentFactory {

    private MonthlyReportV2ContentFactory() {}

    public static MonthlyReportV2Content empty() {
        StyledText oneEmpty = new StyledText(
                List.of(new Segment("", List.of()))
        );
        return new MonthlyReportV2Content("", "", "", "", oneEmpty, oneEmpty);
    }

    public static StyledText fromPlain(String text) {
        return new StyledText(List.of(new Segment(text == null ? "" : text, List.of())));
    }
}
