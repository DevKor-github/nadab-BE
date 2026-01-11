package com.devkor.ifive.nadab.domain.monthlyreport.core.service;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
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
public class PendingMonthlyReportService {

    private final MonthlyReportRepository monthlyReportRepository;

    @Transactional
    public MonthlyReport getOrCreatePendingMonthlyReport(User user) {

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        MonthlyReport report = monthlyReportRepository.findByUserIdAndMonthStartDate(user.getId(), range.monthStartDate())
                .orElseGet(() -> monthlyReportRepository.save(MonthlyReport.createPending(
                        user,
                        range.monthStartDate(),
                        range.monthEndDate(),
                        today
                )));

        if (report.getStatus() == MonthlyReportStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.MONTHLY_REPORT_ALREADY_COMPLETED);
        }

        if (report.getStatus() == MonthlyReportStatus.IN_PROGRESS) {
            throw new ConflictException(ErrorCode.MONTHLY_REPORT_IN_PROGRESS);
        }

        if (report.getStatus() == MonthlyReportStatus.FAILED) {
            monthlyReportRepository.updateStatus(report.getId(), MonthlyReportStatus.PENDING);
        }

        return report;
    }
}
