package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportLocatorResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialRankingItem;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Mock
    private MonthlyReportLocatorResolver monthlyReportLocatorResolver;

    private MonthlyReportQueryServiceV2 service;

    @BeforeEach
    void setUp() {
        service = new MonthlyReportQueryServiceV2(
                monthlyReportRepository,
                monthlyReportV2Repository,
                weeklyReportRepository,
                userRepository,
                profileImageUrlBuilder,
                monthlyReportLocatorResolver
        );
    }

    @Test
    void 현재와_지지난달_월간_리포트_위치_정보를_반환한다() {
        when(userRepository.existsById(1L)).thenReturn(true);
        LocalDate currentMonth = MonthRangeCalculator.getLastMonthRange().monthStartDate();
        LocalDate previousMonth = MonthRangeCalculator.getTwoMonthsAgoRange().monthStartDate();
        MonthlyReportLocatorResponse current = new MonthlyReportLocatorResponse(
                20L, 2, currentMonth.getMonthValue(), MonthlyReportStatus.IN_PROGRESS
        );
        MonthlyReportLocatorResponse previous = new MonthlyReportLocatorResponse(
                10L, 1, previousMonth.getMonthValue(), MonthlyReportStatus.COMPLETED
        );
        when(monthlyReportLocatorResolver.findByMonth(1L, currentMonth)).thenReturn(Optional.of(current));
        when(monthlyReportLocatorResolver.findCompletedByMonth(1L, previousMonth))
                .thenReturn(Optional.of(previous));

        var response = service.getMyMonthlyReport(1L);

        assertThat(response.report()).isEqualTo(current);
        assertThat(response.previousReport()).isEqualTo(previous);
    }

    @Test
    void 연도_경계에서_1월의_이전_리포트로_전년도_12월을_조회한다() {
        MonthRangeDto january = new MonthRangeDto(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        MonthlyReportLocatorResponse current = new MonthlyReportLocatorResponse(
                20L, 2, 1, MonthlyReportStatus.COMPLETED
        );
        MonthlyReportLocatorResponse previous = new MonthlyReportLocatorResponse(
                10L, 1, 12, MonthlyReportStatus.COMPLETED
        );
        when(monthlyReportLocatorResolver.findByMonth(1L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(current));
        when(monthlyReportLocatorResolver.findCompletedByMonth(1L, LocalDate.of(2025, 12, 1)))
                .thenReturn(Optional.of(previous));

        var response = service.getMyMonthlyReport(1L, january);

        assertThat(response.report()).isEqualTo(current);
        assertThat(response.previousReport()).isEqualTo(previous);
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
    void 소셜_요약의_프로필_식별자를_URL로_변환한다() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        MonthlySocialSummaryContent socialSummary = new MonthlySocialSummaryContent(
                true,
                5,
                List.of(new MonthlySocialRankingItem(1, 2L, "가", "custom-key", null, true)),
                List.of(new MonthlySocialRankingItem(1, 3L, "나", null, DefaultProfileType.DEFAULT, true))
        );
        MonthlyReportV2 report = mock(MonthlyReportV2.class);
        when(report.getUser()).thenReturn(user);
        when(report.getMonthStartDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(report.getStatus()).thenReturn(MonthlyReportStatus.COMPLETED);
        when(report.getComparisonType()).thenReturn(MonthlyReportComparisonType.BASELINE);
        when(report.getSocialSummary()).thenReturn(socialSummary);
        when(monthlyReportV2Repository.findById(7L)).thenReturn(Optional.of(report));
        when(profileImageUrlBuilder.buildUrl("custom-key")).thenReturn("https://cdn/custom-key");
        when(profileImageUrlBuilder.buildDefaultUrl(DefaultProfileType.DEFAULT))
                .thenReturn("https://cdn/default/DEFAULT.png");

        MonthlyReportResponseV2 response = service.getMonthlyReportById(1L, 7L);

        assertThat(response.socialSummary().visible()).isTrue();
        assertThat(response.socialSummary().likeRanking()).singleElement().satisfies(item -> {
            assertThat(item.displayOrder()).isEqualTo(1);
            assertThat(item.userId()).isEqualTo(2L);
            assertThat(item.profileImageUrl()).isEqualTo("https://cdn/custom-key");
            assertThat(item.topRank()).isTrue();
        });
        assertThat(response.socialSummary().commentRanking()).singleElement()
                .satisfies(item -> assertThat(item.profileImageUrl())
                        .isEqualTo("https://cdn/default/DEFAULT.png"));
        String responseJson = new ObjectMapper().writeValueAsString(response.socialSummary());
        assertThat(responseJson).contains("profileImageUrl");
        assertThat(responseJson).doesNotContain("profileImageKey", "defaultProfileType");
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
        assertThat(response.socialSummary().visible()).isFalse();
        assertThat(response.socialSummary().month()).isEqualTo(5);
        assertThat(response.socialSummary().likeRanking()).isEmpty();
        assertThat(response.socialSummary().commentRanking()).isEmpty();
    }
}
