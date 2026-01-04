package com.devkor.ifive.nadab.global.exception.report;

import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.Getter;

@Getter
public class WeeklyReportNotEligibleException extends BadRequestException {
    private final CompletedCountResponse completedCountResponse;

    public WeeklyReportNotEligibleException(ErrorCode errorCode, CompletedCountResponse completedCount) {
        super(errorCode);
        this.completedCountResponse = completedCount;
    }
}
