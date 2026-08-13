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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeeklyStatsServiceTest {

    @Test
    void getWeeklyStats_updates_weekly_metric_peaks_from_aggregated_chart_data() {
        WeeklyStatsRepository repo = mock(WeeklyStatsRepository.class);
        WeeklyStatsService service = new WeeklyStatsService(
                repo,
                new PeakStatsTracker(new InMemoryPeakStatsStore())
        );
        LocalDate currentWeekStart = TodayDateTimeProvider.getTodayDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);

        when(repo.findSignupCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new DateCountDto(previousWeekStart.plusDays(1), 4L)));
        when(repo.findAssignedQuestionCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new DateCountDto(currentWeekStart.plusDays(1), 6L)));
        when(repo.findCompletedDailyReportCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new DateCountDto(previousWeekStart, 5L),
                        new DateCountDto(previousWeekStart.plusDays(1), 7L)
                ));
        when(repo.findCompletedWeeklyReportCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new DateCountDto(currentWeekStart, 3L)));
        when(repo.findWeeklyActiveUserCountsByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new DateCountDto(currentWeekStart, 8L)));

        WeeklyStatsViewModel vm = service.getWeeklyStatsLast7Weeks();

        assertThat(vm.signupPeak().value()).isEqualTo(4L);
        assertThat(vm.assignedQuestionPeak().value()).isEqualTo(6L);
        assertThat(vm.completedDailyReportPeak().value()).isEqualTo(12L);
        assertThat(vm.completedDailyReportPeak().periodStart()).isEqualTo(previousWeekStart.toString());
        assertThat(vm.completedWeeklyReportPeak().value()).isEqualTo(3L);
        assertThat(vm.wauPeak().value()).isEqualTo(8L);
        assertThat(vm.wauPeak().currentPeriod()).isTrue();
    }
}
