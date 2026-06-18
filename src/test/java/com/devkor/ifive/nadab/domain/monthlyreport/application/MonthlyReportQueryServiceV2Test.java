package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportQueryServiceV2Test {

    @Mock
    private MonthlyReportRepository monthlyReportRepository;
    @Mock
    private MonthlyReportV2Repository monthlyReportV2Repository;
    @Mock
    private WeeklyReportRepository weeklyReportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileImageUrlBuilder profileImageUrlBuilder;

    private MonthlyReportQueryServiceV2 service;

    @BeforeEach
    void setUp() {
        service = new MonthlyReportQueryServiceV2(
                monthlyReportRepository,
                monthlyReportV2Repository,
                weeklyReportRepository,
                userRepository,
                profileImageUrlBuilder
        );
    }

    @Test
    void COMPARISON_리포트는_감정_비교_스냅샷을_반환한다() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        MonthlyEmotionComparisonContent emotionComparison = new MonthlyEmotionComparisonContent(
                10L,
                4,
                new TypeEmotionStatsContent(10, "ACHIEVEMENT", 57, List.of()),
                14
        );
        MonthlyReportV2 report = mock(MonthlyReportV2.class);
        when(report.getUser()).thenReturn(user);
        when(report.getMonthStartDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(report.getStatus()).thenReturn(MonthlyReportStatus.COMPLETED);
        when(report.getComparisonType()).thenReturn(MonthlyReportComparisonType.COMPARISON);
        when(report.getEmotionComparison()).thenReturn(emotionComparison);
        when(monthlyReportV2Repository.findById(7L)).thenReturn(Optional.of(report));

        MonthlyReportResponseV2 response = service.getMonthlyReportById(1L, 7L);

        assertThat(response.emotionComparison()).isEqualTo(emotionComparison.normalized());
    }

    @Test
    void BASELINE_리포트는_감정_비교_스냅샷을_null로_반환한다() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        MonthlyReportV2 report = mock(MonthlyReportV2.class);
        when(report.getUser()).thenReturn(user);
        when(report.getMonthStartDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(report.getStatus()).thenReturn(MonthlyReportStatus.COMPLETED);
        when(report.getComparisonType()).thenReturn(MonthlyReportComparisonType.BASELINE);
        when(monthlyReportV2Repository.findById(7L)).thenReturn(Optional.of(report));

        MonthlyReportResponseV2 response = service.getMonthlyReportById(1L, 7L);

        assertThat(response.emotionComparison()).isNull();
    }
}
