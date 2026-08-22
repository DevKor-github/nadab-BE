package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.application.helper.PeakStatsTracker;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.WeeklyStatsRepository;
import com.devkor.ifive.nadab.domain.stats.infra.InMemoryPeakStatsStore;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeeklyStatsServiceTest {

    @Test
    void getWeeklyStats_anchors_five_week_chart_and_summary_to_selected_week() {
        WeeklyStatsRepository repo = mock(WeeklyStatsRepository.class);
        WeeklyStatsService service = new WeeklyStatsService(
                repo,
                new PeakStatsTracker(new InMemoryPeakStatsStore())
        );
        LocalDate currentWeekStart = TodayDateTimeProvider.getTodayDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate selectedWeekStart = currentWeekStart.minusWeeks(3);
        LocalDate startWeekStart = selectedWeekStart.minusWeeks(4);
        LocalDate endDateInclusive = selectedWeekStart.plusDays(6);

        when(repo.findSignupCountsByDateBetween(startWeekStart, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedWeekStart.plusDays(1), 4L)));
        when(repo.findAssignedQuestionCountsByDateBetween(startWeekStart, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedWeekStart.plusDays(2), 6L)));
        when(repo.findCompletedDailyReportCountsByDateBetween(startWeekStart, endDateInclusive))
                .thenReturn(List.of(
                        new DateCountDto(selectedWeekStart, 5L),
                        new DateCountDto(selectedWeekStart.plusDays(1), 7L)
                ));
        when(repo.findCompletedWeeklyReportCountsByDateBetween(startWeekStart, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedWeekStart, 3L)));
        when(repo.findWeeklyActiveUserCountsByDateBetween(startWeekStart, endDateInclusive))
                .thenReturn(List.of(new DateCountDto(selectedWeekStart, 8L)));

        WeeklyStatsViewModel vm = service.getWeeklyStats(selectedWeekStart.plusDays(2));

        assertThat(vm.signupPeak().value()).isEqualTo(4L);
        assertThat(vm.assignedQuestionPeak().value()).isEqualTo(6L);
        assertThat(vm.completedDailyReportPeak().value()).isEqualTo(12L);
        assertThat(vm.completedDailyReportPeak().periodStart()).isEqualTo(selectedWeekStart.toString());
        assertThat(vm.completedWeeklyReportPeak().value()).isEqualTo(3L);
        assertThat(vm.wauPeak().value()).isEqualTo(8L);
        assertThat(vm.wauPeak().currentPeriod()).isFalse();
        assertThat(vm.labels()).hasSize(5);
        assertThat(vm.selectedPeriod().periodLabel())
                .isEqualTo(selectedWeekStart + " ~ " + selectedWeekStart.plusDays(6));
        assertThat(vm.selectedPeriod().signupCount()).isEqualTo(4L);
        assertThat(vm.selectedPeriod().assignedQuestionCount()).isEqualTo(6L);
        assertThat(vm.selectedPeriod().completedDailyReportCount()).isEqualTo(12L);
        assertThat(vm.selectedPeriod().completedWeeklyReportCount()).isEqualTo(3L);
        assertThat(vm.selectedPeriod().wauCount()).isEqualTo(8L);

        verify(repo).findSignupCountsByDateBetween(startWeekStart, endDateInclusive);
        verify(repo).findWeeklyActiveUserCountsByDateBetween(startWeekStart, endDateInclusive);
    }
}
