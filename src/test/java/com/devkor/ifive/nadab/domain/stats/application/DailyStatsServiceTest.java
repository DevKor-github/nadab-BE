package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.application.helper.PeakStatsTracker;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.repository.DailyStatsRepository;
import com.devkor.ifive.nadab.domain.stats.infra.InMemoryPeakStatsStore;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyStatsServiceTest {

    @Test
    void getDailyStats_anchors_chart_and_summary_to_selected_date() {
        DailyStatsRepository repo = mock(DailyStatsRepository.class);
        DailyStatsService service = new DailyStatsService(
                repo,
                new PeakStatsTracker(new InMemoryPeakStatsStore())
        );
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate selectedDate = today.minusDays(10);
        LocalDate startDate = selectedDate.minusDays(6);

        when(repo.findSignupCountsLast7Days(startDate, selectedDate))
                .thenReturn(List.<Object[]>of(new Object[]{Date.valueOf(selectedDate), 5L}));
        when(repo.findAssignedQuestionCountsLast7Days(startDate, selectedDate))
                .thenReturn(List.of(new DateCountDto(selectedDate, 7L)));
        when(repo.findCompletedDailyReportCountsLast7Days(startDate, selectedDate))
                .thenReturn(List.of(new DateCountDto(selectedDate, 9L)));
        when(repo.countSharedDailyReports(selectedDate)).thenReturn(2L);

        DailyStatsViewModel vm = service.getDailyStats(selectedDate);

        assertThat(vm.signupPeak().value()).isEqualTo(5L);
        assertThat(vm.signupPeak().periodStart()).isEqualTo(selectedDate.toString());
        assertThat(vm.assignedQuestionPeak().value()).isEqualTo(7L);
        assertThat(vm.dauPeak().value()).isEqualTo(9L);
        assertThat(vm.dauPeak().currentPeriod()).isFalse();
        assertThat(vm.labels()).hasSize(7).endsWith(selectedDate.toString());
        assertThat(vm.selectedPeriod().periodValue()).isEqualTo(selectedDate.toString());
        assertThat(vm.selectedPeriod().signupCount()).isEqualTo(5L);
        assertThat(vm.selectedPeriod().assignedQuestionCount()).isEqualTo(7L);
        assertThat(vm.selectedPeriod().dauCount()).isEqualTo(9L);
        assertThat(vm.selectedPeriod().sharedDailyReportCount()).isEqualTo(2L);

        verify(repo).findSignupCountsLast7Days(startDate, selectedDate);
        verify(repo).findAssignedQuestionCountsLast7Days(startDate, selectedDate);
        verify(repo).findCompletedDailyReportCountsLast7Days(startDate, selectedDate);
    }
}
