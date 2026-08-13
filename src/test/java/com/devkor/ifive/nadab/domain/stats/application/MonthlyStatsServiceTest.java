package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.application.helper.PeakStatsTracker;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.monthly.MonthlyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.MonthlyStatsRepository;
import com.devkor.ifive.nadab.domain.stats.infra.InMemoryPeakStatsStore;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthlyStatsServiceTest {

    @Test
    void getMonthlyStats_combines_v1_and_v2_monthly_report_counts() {
        // given
        MonthlyStatsRepository repo = mock(MonthlyStatsRepository.class);
        MonthlyStatsService service = new MonthlyStatsService(
                repo,
                new PeakStatsTracker(new InMemoryPeakStatsStore())
        );

        YearMonth currentMonth = YearMonth.from(TodayDateTimeProvider.getTodayDate());
        YearMonth previousMonth = currentMonth.minusMonths(1);

        when(repo.findSignupCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(repo.findAssignedQuestionCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(repo.findCompletedDailyReportCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(repo.findMonthlyActiveUserCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(repo.findCompletedMonthlyReportV1CountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new DateCountDto(previousMonth.atDay(15), 2L),
                        new DateCountDto(currentMonth.atDay(1), 3L)
                ));
        when(repo.findCompletedMonthlyReportV2CountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new DateCountDto(previousMonth.atDay(20), 5L),
                        new DateCountDto(currentMonth.atDay(2), 7L)
                ));
        when(repo.countInProgressMonthlyReportV1Now()).thenReturn(1L);
        when(repo.countInProgressMonthlyReportV2Now()).thenReturn(4L);

        // when
        MonthlyStatsViewModel vm = service.getMonthlyStatsLast5Months();

        // then
        assertThat(vm.completedMonthlyReportV1Counts()).containsExactly(0L, 0L, 0L, 2L, 3L);
        assertThat(vm.completedMonthlyReportV2Counts()).containsExactly(0L, 0L, 0L, 5L, 7L);
        assertThat(vm.completedMonthlyReportTotalCounts()).containsExactly(0L, 0L, 0L, 7L, 10L);
        assertThat(vm.completedMonthlyReportPeak().value()).isEqualTo(10L);
        assertThat(vm.completedMonthlyReportPeak().periodLabel()).isEqualTo(currentMonth.toString());
        assertThat(vm.completedMonthlyReportPeak().currentPeriod()).isTrue();
        assertThat(vm.signupPeak().available()).isFalse();
        assertThat(vm.inProgressMonthlyReportV1Count()).isEqualTo(1L);
        assertThat(vm.inProgressMonthlyReportV2Count()).isEqualTo(4L);
        assertThat(vm.inProgressMonthlyReportTotalCount()).isEqualTo(5L);
    }
}
