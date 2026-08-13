package com.devkor.ifive.nadab.domain.stats.core.dto.peak;

public enum PeakMetric {
    DAILY_SIGNUP(PeakPeriodUnit.DAY),
    DAILY_ASSIGNED_QUESTION(PeakPeriodUnit.DAY),
    DAU(PeakPeriodUnit.DAY),
    WEEKLY_SIGNUP(PeakPeriodUnit.WEEK),
    WEEKLY_ASSIGNED_QUESTION(PeakPeriodUnit.WEEK),
    WEEKLY_DAILY_REPORT(PeakPeriodUnit.WEEK),
    WEEKLY_REPORT(PeakPeriodUnit.WEEK),
    WAU(PeakPeriodUnit.WEEK),
    MONTHLY_SIGNUP(PeakPeriodUnit.MONTH),
    MONTHLY_ASSIGNED_QUESTION(PeakPeriodUnit.MONTH),
    MONTHLY_DAILY_REPORT(PeakPeriodUnit.MONTH),
    MONTHLY_REPORT(PeakPeriodUnit.MONTH),
    MAU(PeakPeriodUnit.MONTH);

    private final PeakPeriodUnit periodUnit;

    PeakMetric(PeakPeriodUnit periodUnit) {
        this.periodUnit = periodUnit;
    }

    public PeakPeriodUnit periodUnit() {
        return periodUnit;
    }
}
