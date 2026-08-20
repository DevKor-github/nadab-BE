package com.devkor.ifive.nadab.domain.stats.controller;

import com.devkor.ifive.nadab.domain.admin.infra.security.AdminPageAuthInterceptor;
import com.devkor.ifive.nadab.domain.stats.application.DailyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.MonthlyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TotalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TypeStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WithdrawalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WeeklyStatsService;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.monthly.MonthlyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestSeriesViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
import com.devkor.ifive.nadab.global.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyStatsService dailyStatsService;
    @MockitoBean
    private WeeklyStatsService weeklyStatsService;
    @MockitoBean
    private MonthlyStatsService monthlyStatsService;
    @MockitoBean
    private TotalStatsService totalStatsService;
    @MockitoBean
    private TypeStatsService typeStatsService;
    @MockitoBean
    private WithdrawalStatsService withdrawalStatsService;
    @MockitoBean
    private AdminPageAuthInterceptor adminPageAuthInterceptor;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void dailyStats_renders_peak_value_period_current_badge_and_empty_state() throws Exception {
        PeakStatViewModel currentPeak = peak(1_234L, "2026-08-13", true);
        PeakStatViewModel pastPeak = peak(900L, "2026-08-12", false);
        when(dailyStatsService.getDailyStatsLast7Days()).thenReturn(new DailyStatsViewModel(
                List.of("2026-08-13"),
                List.of(1L),
                List.of(2L),
                List.of(3L),
                currentPeak,
                pastPeak,
                PeakStatViewModel.empty(),
                0L,
                "2026-08-13 12:00:00"
        ));

        mockMvc.perform(get("/stats/daily"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/stats-peak.css")))
                .andExpect(content().string(containsString("역대 최고")))
                .andExpect(content().string(containsString("1,234")))
                .andExpect(content().string(containsString("2026-08-12")))
                .andExpect(content().string(containsString("현재 기간")))
                .andExpect(content().string(containsString("기록 없음")));
    }

    @Test
    void weeklyStats_renders_weekly_peak_period() throws Exception {
        PeakStatViewModel weeklyPeak = peak(80L, "2026-08-10 ~ 2026-08-16", false);
        when(weeklyStatsService.getWeeklyStatsLast7Weeks()).thenReturn(new WeeklyStatsViewModel(
                List.of("08-10 ~ 08-16"),
                List.of(1L),
                List.of(2L),
                List.of(3L),
                List.of(4L),
                List.of(5L),
                weeklyPeak,
                weeklyPeak,
                weeklyPeak,
                weeklyPeak,
                weeklyPeak,
                0L,
                "2026-08-13 12:00:00"
        ));

        mockMvc.perform(get("/stats/weekly"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2026-08-10 ~ 2026-08-16")))
                .andExpect(content().string(containsString("WAU")));
    }

    @Test
    void monthlyStats_renders_monthly_peak_period() throws Exception {
        PeakStatViewModel monthlyPeak = peak(120L, "2026-08", false);
        when(monthlyStatsService.getMonthlyStatsLast5Months()).thenReturn(new MonthlyStatsViewModel(
                List.of("2026-08"),
                List.of(1L),
                List.of(2L),
                List.of(3L),
                List.of(4L),
                List.of(5L),
                List.of(9L),
                List.of(6L),
                monthlyPeak,
                monthlyPeak,
                monthlyPeak,
                monthlyPeak,
                monthlyPeak,
                0L,
                0L,
                0L,
                "2026-08-13 12:00:00"
        ));

        mockMvc.perform(get("/stats/monthly"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2026-08")))
                .andExpect(content().string(containsString("MAU")));
    }

    @Test
    void typeStats_renders_recent_interest_series_and_cumulative_history() throws Exception {
        when(typeStatsService.getTypeStats()).thenReturn(new TypeStatsViewModel(
                2L,
                List.of("취향", "감정"),
                List.of(5L, 3L),
                List.of("2026-08-19", "2026-08-20"),
                List.of(
                        new TypeReportInterestSeriesViewModel("PREFERENCE", "취향", List.of(1L, 4L)),
                        new TypeReportInterestSeriesViewModel("EMOTION", "감정", List.of(2L, 1L))
                ),
                "2026-08-20 12:00:00"
        ));

        mockMvc.perform(get("/stats/type"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("typeTrendChart")))
                .andExpect(content().string(containsString("최근 7일 · 관심사별 COMPLETED 생성 이력")))
                .andExpect(content().string(containsString("재생성 이력 포함")))
                .andExpect(content().string(containsString("2026-08-19")))
                .andExpect(content().string(containsString("PREFERENCE")));
    }

    @Test
    void peakStylesheet_is_served_as_static_resource() throws Exception {
        mockMvc.perform(get("/css/stats-peak.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".peak-record-current")));
    }

    private PeakStatViewModel peak(long value, String periodLabel, boolean currentPeriod) {
        return new PeakStatViewModel(true, value, "2026-08-01", periodLabel, currentPeriod);
    }
}
