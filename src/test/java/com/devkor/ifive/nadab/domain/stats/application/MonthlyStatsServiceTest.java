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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyStatsServiceTest {

    @Test
    void getMonthlyStats_anchors_chart_and_summary_to_selected_month() {
        // given
        MonthlyStatsRepository repo = mock(MonthlyStatsRepository.class);
        MonthlyStatsService service = new MonthlyStatsService(
                repo,
                new PeakStatsTracker(new InMemoryPeakStatsStore())
        );

        YearMonth currentMonth = YearMonth.from(TodayDateTimeProvider.getTodayDate());
        YearMonth selectedMonth = currentMonth.minusMonths(3);
        YearMonth previousMonth = selectedMonth.minusMonths(1);
        LocalDate startDate = selectedMonth.minusMonths(4).atDay(1);
        LocalDate endDateInclusive = selectedMonth.atEndOfMonth();

        when(repo.findSignupCountsByDateBetween(startDate, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedMonth.atDay(1), 11L)));
        when(repo.findAssignedQuestionCountsByDateBetween(startDate, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedMonth.atDay(2), 12L)));
        when(repo.findCompletedDailyReportCountsByDateBetween(startDate, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedMonth.atDay(3), 13L)));
        when(repo.findMonthlyActiveUserCountsByDateBetween(startDate, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedMonth.atDay(1), 14L)));
        when(repo.findCompletedMonthlyReportV1CountsByDateBetween(startDate, endDateInclusive))
                .thenReturn(List.of(
                        new DateCountDto(previousMonth.atDay(15), 2L),
                        new DateCountDto(selectedMonth.atDay(1), 3L)
                ));
        when(repo.findCompletedMonthlyReportV2CountsByDateBetween(startDate, endDateInclusive))
                .thenReturn(List.of(
                        new DateCountDto(previousMonth.atDay(20), 5L),
                        new DateCountDto(selectedMonth.atDay(2), 7L)
                ));
        when(repo.countInProgressMonthlyReportV1Now()).thenReturn(1L);
        when(repo.countInProgressMonthlyReportV2Now()).thenReturn(4L);

        // when
        MonthlyStatsViewModel vm = service.getMonthlyStats(selectedMonth);

        // then
        assertThat(vm.completedMonthlyReportV1Counts()).containsExactly(0L, 0L, 0L, 2L, 3L);
        assertThat(vm.completedMonthlyReportV2Counts()).containsExactly(0L, 0L, 0L, 5L, 7L);
        assertThat(vm.completedMonthlyReportTotalCounts()).containsExactly(0L, 0L, 0L, 7L, 10L);
        assertThat(vm.completedMonthlyReportPeak().value()).isEqualTo(10L);
        assertThat(vm.completedMonthlyReportPeak().periodLabel()).isEqualTo(selectedMonth.toString());
        assertThat(vm.completedMonthlyReportPeak().currentPeriod()).isFalse();
        assertThat(vm.selectedPeriod().periodValue()).isEqualTo(selectedMonth.toString());
        assertThat(vm.selectedPeriod().signupCount()).isEqualTo(11L);
        assertThat(vm.selectedPeriod().assignedQuestionCount()).isEqualTo(12L);
        assertThat(vm.selectedPeriod().completedDailyReportCount()).isEqualTo(13L);
        assertThat(vm.selectedPeriod().completedMonthlyReportV1Count()).isEqualTo(3L);
        assertThat(vm.selectedPeriod().completedMonthlyReportV2Count()).isEqualTo(7L);
        assertThat(vm.selectedPeriod().completedMonthlyReportTotalCount()).isEqualTo(10L);
        assertThat(vm.selectedPeriod().mauCount()).isEqualTo(14L);
        assertThat(vm.inProgressMonthlyReportV1Count()).isEqualTo(1L);
        assertThat(vm.inProgressMonthlyReportV2Count()).isEqualTo(4L);
        assertThat(vm.inProgressMonthlyReportTotalCount()).isEqualTo(5L);

        verify(repo).findSignupCountsByDateBetween(startDate, endDateInclusive);
        verify(repo).findMonthlyActiveUserCountsByDateBetween(startDate, endDateInclusive);
    }
}
