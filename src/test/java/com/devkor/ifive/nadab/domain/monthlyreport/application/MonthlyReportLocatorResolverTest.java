package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportLocatorResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportLocatorResolverTest {

    @Mock
    MonthlyReportRepository monthlyReportRepository;
    @Mock
    MonthlyReportV2Repository monthlyReportV2Repository;

    MonthlyReportLocatorResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MonthlyReportLocatorResolver(monthlyReportRepository, monthlyReportV2Repository);
    }

    @Test
    void resolves_v2_report_without_v1_fallback() {
        LocalDate monthStartDate = LocalDate.of(2026, 5, 1);
        MonthlyReportV2 report = v2Report(20L, monthStartDate, MonthlyReportStatus.IN_PROGRESS);
        when(monthlyReportV2Repository.findByUserIdAndMonthStartDate(1L, monthStartDate))
                .thenReturn(Optional.of(report));

        Optional<MonthlyReportLocatorResponse> result = resolver.findByMonth(1L, monthStartDate);

        assertThat(result).contains(new MonthlyReportLocatorResponse(
                20L, 2, 5, MonthlyReportStatus.IN_PROGRESS
        ));
        verifyNoInteractions(monthlyReportRepository);
    }

    @Test
    void falls_back_to_v1_when_v2_report_does_not_exist() {
        LocalDate monthStartDate = LocalDate.of(2026, 5, 1);
        MonthlyReport report = v1Report(10L, monthStartDate, MonthlyReportStatus.COMPLETED);
        when(monthlyReportV2Repository.findByUserIdAndMonthStartDate(1L, monthStartDate))
                .thenReturn(Optional.empty());
        when(monthlyReportRepository.findByUserIdAndMonthStartDate(1L, monthStartDate))
                .thenReturn(Optional.of(report));

        Optional<MonthlyReportLocatorResponse> result = resolver.findByMonth(1L, monthStartDate);

        assertThat(result).contains(new MonthlyReportLocatorResponse(
                10L, 1, 5, MonthlyReportStatus.COMPLETED
        ));
    }

    @Test
    void resolves_previous_report_with_completed_status_only() {
        LocalDate monthStartDate = LocalDate.of(2026, 4, 1);
        MonthlyReport report = v1Report(10L, monthStartDate, MonthlyReportStatus.COMPLETED);
        when(monthlyReportV2Repository.findByUserIdAndMonthStartDateAndStatus(
                1L, monthStartDate, MonthlyReportStatus.COMPLETED
        )).thenReturn(Optional.empty());
        when(monthlyReportRepository.findByUserIdAndMonthStartDateAndStatus(
                1L, monthStartDate, MonthlyReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));

        Optional<MonthlyReportLocatorResponse> result = resolver.findCompletedByMonth(1L, monthStartDate);

        assertThat(result).contains(new MonthlyReportLocatorResponse(
                10L, 1, 4, MonthlyReportStatus.COMPLETED
        ));
        verify(monthlyReportV2Repository).findByUserIdAndMonthStartDateAndStatus(
                1L, monthStartDate, MonthlyReportStatus.COMPLETED
        );
        verify(monthlyReportRepository).findByUserIdAndMonthStartDateAndStatus(
                1L, monthStartDate, MonthlyReportStatus.COMPLETED
        );
    }

    private MonthlyReportV2 v2Report(Long id, LocalDate monthStartDate, MonthlyReportStatus status) {
        MonthlyReportV2 report = mock(MonthlyReportV2.class);
        when(report.getId()).thenReturn(id);
        when(report.getMonthStartDate()).thenReturn(monthStartDate);
        when(report.getStatus()).thenReturn(status);
        return report;
    }

    private MonthlyReport v1Report(Long id, LocalDate monthStartDate, MonthlyReportStatus status) {
        MonthlyReport report = mock(MonthlyReport.class);
        when(report.getId()).thenReturn(id);
        when(report.getMonthStartDate()).thenReturn(monthStartDate);
        when(report.getStatus()).thenReturn(status);
        return report;
    }
}
