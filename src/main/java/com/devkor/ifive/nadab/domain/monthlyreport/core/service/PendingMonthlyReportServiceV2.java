package com.devkor.ifive.nadab.domain.monthlyreport.core.service;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PendingMonthlyReportServiceV2 {

    private final MonthlyReportV2Repository monthlyReportV2Repository;
    private final MonthlyReportCrossVersionGuardService monthlyReportCrossVersionGuardService;

    @Transactional
    public MonthlyReportV2 getOrCreatePendingMonthlyReport(User user, boolean exists) {

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        monthlyReportCrossVersionGuardService.validateCreatableForV2(user.getId(), range.monthStartDate());

        MonthlyReportComparisonType comparisonType = exists ? MonthlyReportComparisonType.COMPARISON : MonthlyReportComparisonType.BASELINE;

        MonthlyReportV2 report = monthlyReportV2Repository.findByUserIdAndMonthStartDate(user.getId(), range.monthStartDate())
                .orElseGet(() -> monthlyReportV2Repository.save(MonthlyReportV2.createPending(
                        user,
                        range.monthStartDate(),
                        range.monthEndDate(),
                        today,
                        comparisonType
                )));

        if (report.getStatus() == MonthlyReportStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.MONTHLY_REPORT_ALREADY_COMPLETED);
        }

        if (report.getStatus() == MonthlyReportStatus.IN_PROGRESS
            || report.getStatus() == MonthlyReportStatus.TEXT_COMPLETED) {
            throw new ConflictException(ErrorCode.MONTHLY_REPORT_IN_PROGRESS);
        }

        if (report.getStatus() == MonthlyReportStatus.FAILED) {
            monthlyReportV2Repository.updateStatus(report.getId(), MonthlyReportStatus.PENDING);
        }

        return report;
    }
}
