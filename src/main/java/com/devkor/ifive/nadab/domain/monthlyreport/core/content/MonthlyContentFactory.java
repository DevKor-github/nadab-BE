package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import java.util.List;

public final class MonthlyContentFactory {

    private MonthlyContentFactory() {
    }

    public static InterestStatsContent emptyInterestStats() {
        return new InterestStatsContent(List.of()).normalized();
    }
}
