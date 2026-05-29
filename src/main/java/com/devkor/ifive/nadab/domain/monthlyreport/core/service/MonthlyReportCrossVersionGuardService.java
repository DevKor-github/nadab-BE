package com.devkor.ifive.nadab.domain.monthlyreport.core.service;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MonthlyReportCrossVersionGuardService {

    private final MonthlyReportRepository monthlyReportRepository;
    private final MonthlyReportV2Repository monthlyReportV2Repository;

    public void validateCreatableForV1(Long userId, LocalDate monthStartDate) {
        monthlyReportV2Repository.findByUserIdAndMonthStartDate(userId, monthStartDate)
                .ifPresent(reportV2 -> {
                    if (reportV2.getStatus() == MonthlyReportStatus.COMPLETED) {
                        throw new ConflictException(ErrorCode.MONTHLY_REPORT_ALREADY_COMPLETED);
                    }

                    if (reportV2.getStatus() == MonthlyReportStatus.IN_PROGRESS) {
                        throw new ConflictException(ErrorCode.MONTHLY_REPORT_IN_PROGRESS);
                    }
                });
    }

    public void validateCreatableForV2(Long userId, LocalDate monthStartDate) {
        monthlyReportRepository.findByUserIdAndMonthStartDate(userId, monthStartDate)
                .ifPresent(report -> {
                    if (report.getStatus() == MonthlyReportStatus.COMPLETED) {
                        throw new ConflictException(ErrorCode.MONTHLY_REPORT_ALREADY_COMPLETED);
                    }

                    if (report.getStatus() == MonthlyReportStatus.IN_PROGRESS) {
                        throw new ConflictException(ErrorCode.MONTHLY_REPORT_IN_PROGRESS);
                    }
                });
    }
}
