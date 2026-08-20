package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportDateInterestCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestSeriesViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.TypeStatsRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TypeStatsServiceTest {

    @Test
    void getTypeStats_fills_missing_dates_and_interests_with_zero() {
        TypeStatsRepository repo = mock(TypeStatsRepository.class);
        TypeStatsService service = new TypeStatsService(repo);
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        when(repo.countInProgressTypeReportsNow()).thenReturn(2L);
        when(repo.countCompletedTypeReportsByInterest()).thenReturn(List.of(
                new TypeReportInterestCountDto(InterestCode.PREFERENCE, 5L),
                new TypeReportInterestCountDto(InterestCode.EMOTION, 3L)
        ));
        when(repo.countCompletedTypeReportsByDateAndInterest(today.minusDays(6), today)).thenReturn(List.of(
                new TypeReportDateInterestCountDto(today.minusDays(1), InterestCode.EMOTION, 2L),
                new TypeReportDateInterestCountDto(today, InterestCode.PREFERENCE, 4L)
        ));

        TypeStatsViewModel vm = service.getTypeStats();

        assertThat(vm.inProgressTypeReportCount()).isEqualTo(2L);
        assertThat(vm.interestLabels()).containsExactly("취향", "감정", "루틴", "인간관계", "사랑", "가치관");
        assertThat(vm.completedTypeReportCounts()).containsExactly(5L, 3L, 0L, 0L, 0L, 0L);
        assertThat(vm.recentDateLabels()).containsExactly(
                today.minusDays(6).toString(),
                today.minusDays(5).toString(),
                today.minusDays(4).toString(),
                today.minusDays(3).toString(),
                today.minusDays(2).toString(),
                today.minusDays(1).toString(),
                today.toString()
        );

        Map<String, TypeReportInterestSeriesViewModel> seriesByInterest = vm.completedTypeReportSeries().stream()
                .collect(Collectors.toMap(TypeReportInterestSeriesViewModel::interestCode, Function.identity()));

        assertThat(seriesByInterest.get("PREFERENCE").counts())
                .containsExactly(0L, 0L, 0L, 0L, 0L, 0L, 4L);
        assertThat(seriesByInterest.get("EMOTION").counts())
                .containsExactly(0L, 0L, 0L, 0L, 0L, 2L, 0L);
        assertThat(seriesByInterest.get("ROUTINE").counts())
                .containsExactly(0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }
}
