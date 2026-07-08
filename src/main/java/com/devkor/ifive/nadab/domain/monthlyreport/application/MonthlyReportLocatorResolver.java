package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportLocatorResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MonthlyReportLocatorResolver {

    private final MonthlyReportRepository monthlyReportRepository;
    private final MonthlyReportV2Repository monthlyReportV2Repository;

    public Optional<MonthlyReportLocatorResponse> findByMonth(Long userId, LocalDate monthStartDate) {
        return monthlyReportV2Repository.findByUserIdAndMonthStartDate(userId, monthStartDate)
                .map(this::toLocatorResponse)
                .or(() -> monthlyReportRepository.findByUserIdAndMonthStartDate(userId, monthStartDate)
                        .map(this::toLocatorResponse));
    }

    public Optional<MonthlyReportLocatorResponse> findCompletedByMonth(Long userId, LocalDate monthStartDate) {
        return monthlyReportV2Repository.findByUserIdAndMonthStartDateAndStatus(
                        userId,
                        monthStartDate,
                        MonthlyReportStatus.COMPLETED
                )
                .map(this::toLocatorResponse)
                .or(() -> monthlyReportRepository.findByUserIdAndMonthStartDateAndStatus(
                                userId,
                                monthStartDate,
                                MonthlyReportStatus.COMPLETED
                        )
                        .map(this::toLocatorResponse));
    }

    private MonthlyReportLocatorResponse toLocatorResponse(MonthlyReportV2 report) {
        return new MonthlyReportLocatorResponse(
                report.getId(),
                2,
                report.getMonthStartDate().getMonthValue(),
                report.getStatus() == null ? MonthlyReportStatus.PENDING : report.getStatus()
        );
    }

    private MonthlyReportLocatorResponse toLocatorResponse(MonthlyReport report) {
        return new MonthlyReportLocatorResponse(
                report.getId(),
                1,
                report.getMonthStartDate().getMonthValue(),
                report.getStatus() == null ? MonthlyReportStatus.PENDING : report.getStatus()
        );
    }
}
