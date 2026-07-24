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

    public Optional<MonthlyReportLocatorResponse> findLatestCompletedBefore(Long userId, LocalDate monthStartDate) {
        Optional<MonthlyReportV2> v2Report = monthlyReportV2Repository
                .findFirstByUserIdAndStatusAndMonthStartDateBeforeOrderByMonthStartDateDesc(
                        userId,
                        MonthlyReportStatus.COMPLETED,
                        monthStartDate
                );
        Optional<MonthlyReport> v1Report = monthlyReportRepository
                .findFirstByUserIdAndStatusAndMonthStartDateBeforeOrderByMonthStartDateDesc(
                        userId,
                        MonthlyReportStatus.COMPLETED,
                        monthStartDate
                );

        if (v2Report.isPresent() && v1Report.isPresent()) {
            if (v2Report.get().getMonthStartDate().isAfter(v1Report.get().getMonthStartDate())) {
                return v2Report.map(this::toLocatorResponse);
            }
            return v1Report.map(this::toLocatorResponse);
        }

        return v2Report.map(this::toLocatorResponse)
                .or(() -> v1Report.map(this::toLocatorResponse));
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
