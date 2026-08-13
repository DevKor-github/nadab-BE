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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DailyStatsServiceTest {

    @Test
    void getDailyStats_updates_daily_metric_peaks_from_chart_data() {
        DailyStatsRepository repo = mock(DailyStatsRepository.class);
        DailyStatsService service = new DailyStatsService(
                repo,
                new PeakStatsTracker(new InMemoryPeakStatsStore())
        );
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate twoDaysAgo = today.minusDays(2);

        when(repo.findSignupCountsLast7Days(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.<Object[]>of(new Object[]{Date.valueOf(twoDaysAgo), 5L}));
        when(repo.findAssignedQuestionCountsLast7Days(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new DateCountDto(today.minusDays(1), 7L)));
        when(repo.findCompletedDailyReportCountsLast7Days(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new DateCountDto(today, 9L)));

        DailyStatsViewModel vm = service.getDailyStatsLast7Days();

        assertThat(vm.signupPeak().value()).isEqualTo(5L);
        assertThat(vm.signupPeak().periodStart()).isEqualTo(twoDaysAgo.toString());
        assertThat(vm.assignedQuestionPeak().value()).isEqualTo(7L);
        assertThat(vm.dauPeak().value()).isEqualTo(9L);
        assertThat(vm.dauPeak().currentPeriod()).isTrue();
    }
}
