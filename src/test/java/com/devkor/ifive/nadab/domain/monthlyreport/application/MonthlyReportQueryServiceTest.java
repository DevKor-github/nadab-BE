package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportQueryServiceTest {

    @Mock
    MonthlyReportRepository monthlyReportRepository;
    @Mock
    UserRepository userRepository;

    MonthlyReportQueryService service;

    @BeforeEach
    void setUp() {
        service = new MonthlyReportQueryService(monthlyReportRepository, userRepository);
    }

    @Test
    void returns_owned_monthly_report() {
        MonthlyReport report = report(1L);
        when(report.getMonthStartDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(report.getStatus()).thenReturn(MonthlyReportStatus.COMPLETED);
        when(monthlyReportRepository.findById(10L)).thenReturn(Optional.of(report));

        var response = service.getMonthlyReportById(1L, 10L);

        assertThat(response.month()).isEqualTo(5);
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void rejects_other_users_monthly_report() {
        MonthlyReport report = report(2L);
        when(monthlyReportRepository.findById(10L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.getMonthlyReportById(1L, 10L))
                .isInstanceOfSatisfying(ForbiddenException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MONTHLY_REPORT_ACCESS_FORBIDDEN));
    }

    @Test
    void rejects_missing_monthly_report() {
        when(monthlyReportRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMonthlyReportById(1L, 10L))
                .isInstanceOfSatisfying(NotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MONTHLY_REPORT_NOT_FOUND));
    }

    private MonthlyReport report(Long ownerId) {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(ownerId);
        MonthlyReport report = mock(MonthlyReport.class);
        when(report.getUser()).thenReturn(owner);
        return report;
    }
}
