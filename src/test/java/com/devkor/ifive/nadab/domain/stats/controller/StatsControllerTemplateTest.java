package com.devkor.ifive.nadab.domain.stats.controller;

import com.devkor.ifive.nadab.domain.admin.infra.security.AdminPageAuthInterceptor;
import com.devkor.ifive.nadab.domain.stats.application.AskChatStatsService;
import com.devkor.ifive.nadab.domain.stats.application.DailyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.MonthlyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.QuestionStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TotalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TypeStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WithdrawalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WeeklyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.helper.DailyQuestionOverviewCsvExporter;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyWalletStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSourceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatWalletStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyPeriodStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.monthly.MonthlyPeriodStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.monthly.MonthlyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewQuery;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSort;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSortDirection;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionReactionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionRevisionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestSeriesViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyPeriodStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DailyQuestionOverviewCsvExporter.class)
class StatsControllerTemplateTest {

    private static final OffsetDateTime ANALYTICS_BASELINE =
            OffsetDateTime.parse("2026-08-25T12:00:00+09:00");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AskChatStatsService askChatStatsService;
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
    private QuestionStatsService questionStatsService;
    @MockitoBean
    private WithdrawalStatsService withdrawalStatsService;
    @MockitoBean
    private AdminPageAuthInterceptor adminPageAuthInterceptor;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void dailyStats_renders_peak_value_period_current_badge_and_empty_state() throws Exception {
        LocalDate selectedDate = LocalDate.of(2026, 8, 13);
        PeakStatViewModel currentPeak = peak(1_234L, "2026-08-13", true);
        PeakStatViewModel pastPeak = peak(900L, "2026-08-12", false);
        when(dailyStatsService.getDailyStats(selectedDate)).thenReturn(new DailyStatsViewModel(
                List.of("2026-08-13"),
                List.of(1L),
                List.of(2L),
                List.of(3L),
                new DailyPeriodStatsViewModel("2026-08-13", "2026-08-13", 1L, 2L, 3L, 0L),
                currentPeak,
                pastPeak,
                PeakStatViewModel.empty(),
                4_321L,
                "2026-08-13 12:00:00"
        ));

        String html = mockMvc.perform(get("/stats/daily").param("date", "2026-08-13"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/stats-peak.css")))
                .andExpect(content().string(containsString("/css/stats-period.css")))
                .andExpect(content().string(containsString("선택한 일간 통계")))
                .andExpect(content().string(containsString("name=\"date\"")))
                .andExpect(content().string(containsString("type=\"date\"")))
                .andExpect(content().string(containsString("value=\"2026-08-13\"")))
                .andExpect(content().string(containsString("할당된 질문 수")))
                .andExpect(content().string(containsString("DAU · 일간 리포트 작성자")))
                .andExpect(content().string(containsString("현재 공유 중인 일간 리포트")))
                .andExpect(content().string(containsString("4,321")))
                .andExpect(content().string(containsString("공유 중인 일간 리포트")))
                .andExpect(content().string(containsString("역대 최고")))
                .andExpect(content().string(containsString("1,234")))
                .andExpect(content().string(containsString("2026-08-12")))
                .andExpect(content().string(containsString("현재 기간")))
                .andExpect(content().string(containsString("기록 없음")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html.indexOf("현재 공유 중인 일간 리포트"))
                .isLessThan(html.indexOf("<canvas id=\"signupChart\""));
        assertThat(html.indexOf("<canvas id=\"completedChart\""))
                .isLessThan(html.indexOf("선택한 일간 통계"));
    }

    @Test
    void weeklyStats_renders_weekly_peak_period() throws Exception {
        LocalDate selectedWeekStart = LocalDate.of(2026, 8, 10);
        PeakStatViewModel weeklyPeak = peak(80L, "2026-08-10 ~ 2026-08-16", false);
        when(weeklyStatsService.getWeeklyStats(selectedWeekStart)).thenReturn(new WeeklyStatsViewModel(
                List.of("08-10 ~ 08-16"),
                List.of(1L),
                List.of(2L),
                List.of(3L),
                List.of(4L),
                List.of(5L),
                new WeeklyPeriodStatsViewModel(
                        "2026-W33", "2026-08-10 ~ 2026-08-16", 1L, 2L, 3L, 4L, 5L
                ),
                weeklyPeak,
                weeklyPeak,
                weeklyPeak,
                weeklyPeak,
                weeklyPeak,
                0L,
                "2026-08-13 12:00:00"
        ));

        String html = mockMvc.perform(get("/stats/weekly").param("week", "2026-W33"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("선택한 주간 통계")))
                .andExpect(content().string(containsString("name=\"week\"")))
                .andExpect(content().string(containsString("type=\"week\"")))
                .andExpect(content().string(containsString("value=\"2026-W33\"")))
                .andExpect(content().string(containsString("2026-08-10 ~ 2026-08-16")))
                .andExpect(content().string(containsString("생성된 주간 리포트")))
                .andExpect(content().string(containsString("WAU")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html.indexOf("<canvas id=\"wauChart\""))
                .isLessThan(html.indexOf("선택한 주간 통계"));
    }

    @Test
    void askChatStats_renders_usage_quality_wallet_and_rag_dashboard() throws Exception {
        AskChatDailyStatsViewModel dailyStats = new AskChatDailyStatsViewModel(
                LocalDate.of(2026, 8, 30),
                4L,
                3L,
                2L,
                2L,
                8L,
                7L,
                1L,
                320.5,
                780L,
                new AskChatDailyWalletStatsViewModel(8L, 1L, 6L, 1L, 5L, 2L, 3L, 0L, 1L, 4L, 3L, -1L),
                new AskChatDailyRagStatsViewModel(5L, 1L, 3L, 1L, 0L, 1.0, 4L, 3L)
        );
        AskChatStatsViewModel stats = new AskChatStatsViewModel(
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 31),
                List.of(dailyStats),
                10L,
                8L,
                4L,
                6L,
                20L,
                18L,
                2L,
                90.0,
                320.5,
                780L,
                List.of(new AskChatErrorStatsDto("TIMEOUT", 2L)),
                new AskChatWalletStatsViewModel(20L, 1L, 17L, 2L, 12L, 5L, 8L, 1L, 0L, 9L, 7L, -3L),
                new AskChatRagStatsViewModel(
                        12L,
                        2L,
                        8L,
                        1L,
                        1L,
                        66.7,
                        1.5,
                        14L,
                        9L,
                        List.of(new AskChatRagSourceStatsDto("ASK_CHAT_MESSAGE", 12L, 2L, 8L, 1L, 1L)),
                        List.of(new AskChatRagErrorStatsDto("EMBEDDING_TIMEOUT", 1L))
                ),
                "2026-08-31 12:00:00"
        );
        when(askChatStatsService.getAskChatStats()).thenReturn(stats);

        String html = mockMvc.perform(get("/stats/ask-chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ask Chat 집계 기간")))
                .andExpect(content().string(containsString("2026-08-25 ~ 2026-08-31")))
                .andExpect(content().string(containsString("usageChart")))
                .andExpect(content().string(containsString("답변 성공률")))
                .andExpect(content().string(containsString("대화권 현황")))
                .andExpect(content().string(containsString("유료 충전")))
                .andExpect(content().string(containsString("RAG 현황")))
                .andExpect(content().string(containsString("ASK_CHAT_MESSAGE")))
                .andExpect(content().string(containsString("EMBEDDING_TIMEOUT")))
                .andExpect(content().string(containsString("class=\"tab-link  active\"")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("일별 Ask Chat 상세", "TIMEOUT", "90.0%", "320.5 ms", "780 ms")
                .contains("href=\"/stats/ask-chat\"");
        verify(askChatStatsService).getAskChatStats();
    }

    @Test
    void monthlyStats_renders_monthly_peak_period() throws Exception {
        YearMonth selectedMonth = YearMonth.of(2026, 8);
        PeakStatViewModel monthlyPeak = peak(120L, "2026-08", false);
        when(monthlyStatsService.getMonthlyStats(selectedMonth)).thenReturn(new MonthlyStatsViewModel(
                List.of("2026-08"),
                List.of(1L),
                List.of(2L),
                List.of(3L),
                List.of(4L),
                List.of(5L),
                List.of(9L),
                List.of(6L),
                new MonthlyPeriodStatsViewModel(
                        "2026-08", "2026-08", 1L, 2L, 3L, 4L, 5L, 9L, 6L
                ),
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

        String html = mockMvc.perform(get("/stats/monthly").param("month", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("선택한 월간 통계")))
                .andExpect(content().string(containsString("name=\"month\"")))
                .andExpect(content().string(containsString("type=\"month\"")))
                .andExpect(content().string(containsString("value=\"2026-08\"")))
                .andExpect(content().string(containsString("2026-08")))
                .andExpect(content().string(containsString("생성된 월간 리포트 · Total")))
                .andExpect(content().string(containsString("MAU")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html.indexOf("<canvas id=\"mauChart\""))
                .isLessThan(html.indexOf("선택한 월간 통계"));
    }

    @Test
    void statsPages_reject_invalid_or_future_period_parameters() throws Exception {
        mockMvc.perform(get("/stats/daily").param("date", "2999-01-01"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/weekly").param("week", "invalid"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/monthly").param("month", "2026-13"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/question").param("questionId", "not-a-number"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/question/overview").param("questionLevel", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/question/overview").param("questionLevel", "6"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/question/overview").param("minimumCurrentExposureCount", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/question/overview").param("sort", "INVALID"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/stats/question/overview.csv").param("questionLevel", "6"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void questionOverview_renders_filters_sortable_table_and_detail_links() throws Exception {
        OffsetDateTime effectiveFrom = OffsetDateTime.parse("2026-08-26T12:00:00+09:00");
        DailyQuestionOverviewQuery query = DailyQuestionOverviewQuery.defaults();
        DailyQuestionOverviewRowViewModel question = new DailyQuestionOverviewRowViewModel(
                42L,
                InterestCode.PREFERENCE,
                "현재 질문 문구",
                2,
                2,
                null,
                effectiveFrom,
                10L,
                4L,
                3L,
                3L,
                15L,
                6L,
                4L,
                5L
        );
        when(questionStatsService.getQuestionOverview(query)).thenReturn(new DailyQuestionOverviewViewModel(
                List.of(question),
                875,
                query,
                ANALYTICS_BASELINE,
                "2026-08-26 12:30:00"
        ));

        mockMvc.perform(get("/stats/question/overview"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("전체 질문 현황")))
                .andExpect(content().string(containsString("name=\"keyword\"")))
                .andExpect(content().string(containsString("name=\"interestCode\"")))
                .andExpect(content().string(containsString("name=\"questionLevel\"")))
                .andExpect(content().string(containsString("name=\"active\"")))
                .andExpect(content().string(containsString("name=\"minimumCurrentExposureCount\"")))
                .andExpect(content().string(containsString("name=\"sort\"")))
                .andExpect(content().string(containsString("name=\"direction\"")))
                .andExpect(content().string(containsString("875")))
                .andExpect(content().string(containsString("CURRENT REVISION")))
                .andExpect(content().string(containsString("현재 Revision 반응")))
                .andExpect(content().string(containsString("전체 Revision 누적")))
                .andExpect(content().string(containsString("현재 질문 문구")))
                .andExpect(content().string(containsString("#42")))
                .andExpect(content().string(containsString("40.0%")))
                .andExpect(content().string(containsString("30.0%")))
                .andExpect(content().string(containsString("data-sort=\"CURRENT_REROLL_RATE\"")))
                .andExpect(content().string(containsString("CSV 다운로드")))
                .andExpect(content().string(containsString("href=\"/stats/question/overview.csv")))
                .andExpect(content().string(containsString("href=\"/stats/question?questionId=42\"")))
                .andExpect(content().string(containsString("집계 시작 기준")))
                .andExpect(content().string(containsString("질문 반응 추적 기능이 배포된 시점 이후")))
                .andExpect(content().string(containsString("(2026년 8월 25일 12:00 KST)")))
                .andExpect(content().string(containsString("class=\"tab-link  active\"")));
    }

    @Test
    void questionOverview_preserves_filters_and_sort_order() throws Exception {
        DailyQuestionOverviewQuery query = new DailyQuestionOverviewQuery(
                "노래",
                InterestCode.EMOTION,
                2,
                false,
                10L,
                DailyQuestionOverviewSort.CURRENT_REROLL_RATE,
                DailyQuestionOverviewSortDirection.ASC
        );
        when(questionStatsService.getQuestionOverview(query)).thenReturn(new DailyQuestionOverviewViewModel(
                List.of(),
                875,
                query,
                ANALYTICS_BASELINE,
                "2026-08-26 12:30:00"
        ));

        String html = mockMvc.perform(get("/stats/question/overview")
                        .param("keyword", "노래")
                        .param("interestCode", "EMOTION")
                        .param("questionLevel", "2")
                        .param("active", "false")
                        .param("minimumCurrentExposureCount", "10")
                        .param("sort", "CURRENT_REROLL_RATE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"노래\"")))
                .andExpect(content().string(containsString("value=\"10\"")))
                .andExpect(content().string(containsString("조건에 맞는 질문이 없습니다.")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .containsPattern("<option value=\"EMOTION\"\\s+selected=\"selected\">감정</option>")
                .containsPattern("<option value=\"2\"\\s+selected=\"selected\">LEVEL 2</option>")
                .containsPattern("<option value=\"false\"\\s+selected=\"selected\">INACTIVE</option>")
                .containsPattern("<option value=\"CURRENT_REROLL_RATE\"\\s+selected=\"selected\">현재 Rev 교체율</option>")
                .containsPattern("<option value=\"ASC\"\\s+selected=\"selected\">오름차순</option>")
                .contains(
                        "--sort-ascending: #ff5c68",
                        ".question-table.sort-ascending .sort-indicator",
                        "question-table  sort-ascending",
                        "sort-indicator\">↑",
                        "interestCode=EMOTION",
                        "questionLevel=2",
                        "active=false",
                        "minimumCurrentExposureCount=10",
                        "sort=CURRENT_REROLL_RATE",
                        "direction=ASC"
                );

        verify(questionStatsService).getQuestionOverview(query);
    }

    @Test
    void questionOverviewCsv_downloads_filtered_and_sorted_rows() throws Exception {
        DailyQuestionOverviewQuery query = new DailyQuestionOverviewQuery(
                "노래",
                InterestCode.EMOTION,
                2,
                false,
                10L,
                DailyQuestionOverviewSort.CURRENT_REROLL_RATE,
                DailyQuestionOverviewSortDirection.ASC
        );
        DailyQuestionOverviewRowViewModel question = new DailyQuestionOverviewRowViewModel(
                42L,
                InterestCode.EMOTION,
                "노래 질문",
                2,
                3,
                OffsetDateTime.parse("2026-08-27T09:00:00+09:00"),
                OffsetDateTime.parse("2026-08-26T12:00:00+09:00"),
                10L,
                4L,
                3L,
                3L,
                15L,
                6L,
                4L,
                5L
        );
        when(questionStatsService.getQuestionOverview(query)).thenReturn(new DailyQuestionOverviewViewModel(
                List.of(question),
                875,
                query,
                ANALYTICS_BASELINE,
                "2026-08-27 10:00:00"
        ));

        byte[] csvBytes = mockMvc.perform(get("/stats/question/overview.csv")
                        .param("keyword", "노래")
                        .param("interestCode", "EMOTION")
                        .param("questionLevel", "2")
                        .param("active", "false")
                        .param("minimumCurrentExposureCount", "10")
                        .param("sort", "CURRENT_REROLL_RATE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"nadab_daily_question_stats.csv\""
                ))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(csvBytes).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(new String(csvBytes, StandardCharsets.UTF_8))
                .contains("질문 ID,질문 문구")
                .contains("42,\"노래 질문\",\"EMOTION\",\"감정\"")
                .contains(",10,4,40.0,3,30.0,3,15,6,40.0,4,5\r\n");
        verify(questionStatsService).getQuestionOverview(query);
    }

    @Test
    void questionStats_renders_selector_totals_and_revision_history() throws Exception {
        OffsetDateTime effectiveFrom = OffsetDateTime.parse("2026-08-26T12:00:00+09:00");
        DailyQuestionListItemViewModel selectedQuestion = new DailyQuestionListItemViewModel(
                42L,
                InterestCode.PREFERENCE,
                "현재 질문 문구",
                2,
                2,
                null
        );
        DailyQuestionRevisionStatsViewModel revision = new DailyQuestionRevisionStatsViewModel(
                102L,
                2,
                InterestCode.PREFERENCE,
                "수정된 질문 문구",
                2,
                "공감 가이드",
                "힌트 가이드",
                "리딩 질문 가이드",
                null,
                effectiveFrom,
                "V20260826_1200",
                10L,
                4L,
                3L,
                3L
        );
        when(questionStatsService.getQuestionStats(42L)).thenReturn(new DailyQuestionStatsViewModel(
                List.of(
                        selectedQuestion,
                        new DailyQuestionListItemViewModel(
                                43L,
                                InterestCode.EMOTION,
                                "다른 질문 문구",
                                1,
                                1,
                                null
                        )
                ),
                selectedQuestion,
                new DailyQuestionReactionStatsViewModel(15L, 6L, 4L, 5L),
                List.of(revision),
                ANALYTICS_BASELINE,
                "2026-08-26 12:30:00"
        ));

        String html = mockMvc.perform(get("/stats/question").param("questionId", "42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"questionId\"")))
                .andExpect(content().string(containsString("id=\"questionSearch\"")))
                .andExpect(content().string(containsString("id=\"questionSelect\"")))
                .andExpect(content().string(containsString("id=\"questionSearchStatus\"")))
                .andExpect(content().string(containsString("id=\"questionSearchResults\"")))
                .andExpect(content().string(containsString("검색 결과가 없습니다.")))
                .andExpect(content().string(containsString("QUESTION #42")))
                .andExpect(content().string(containsString("누적 노출")))
                .andExpect(content().string(containsString("누적 답변")))
                .andExpect(content().string(containsString("40.0%")))
                .andExpect(content().string(containsString("Revision 2")))
                .andExpect(content().string(containsString("V20260826_1200")))
                .andExpect(content().string(containsString("공감 가이드")))
                .andExpect(content().string(containsString("집계 시작 기준")))
                .andExpect(content().string(containsString("질문 반응 추적 기능이 배포된 시점 이후")))
                .andExpect(content().string(containsString("(2026년 8월 25일 12:00 KST)")))
                .andExpect(content().string(containsString("배포 전 할당·답변은 추정하거나 현재 수정 버전에 소급하지 않습니다.")))
                .andExpect(content().string(containsString("href=\"/stats/question\"")))
                .andExpect(content().string(containsString("active\" href=\"/stats/question\"")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains(
                        "이 페이지를 읽는 방법",
                        "Question ID (질문 ID)",
                        "Revision (수정 버전)",
                        "교체가 곧 불호를 뜻하지는 않습니다.",
                        "예시로 이해하기",
                        "해석할 때 주의할 점",
                        "질문 반응 추적 기능 배포 전 데이터는 소급하지 않았습니다."
                );
        assertThat(html.indexOf("Revision별 반응 및 수정 이력"))
                .isLessThan(html.indexOf("이 페이지를 읽는 방법"));
    }

    @Test
    void questionStats_renders_empty_state_for_unknown_question() throws Exception {
        when(questionStatsService.getQuestionStats(999L)).thenReturn(new DailyQuestionStatsViewModel(
                List.of(),
                null,
                new DailyQuestionReactionStatsViewModel(0L, 0L, 0L, 0L),
                List.of(),
                ANALYTICS_BASELINE,
                "2026-08-26 12:30:00"
        ));

        mockMvc.perform(get("/stats/question").param("questionId", "999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("해당 ID의 질문을 찾을 수 없습니다.")));
    }

    @Test
    void all_stats_templates_use_common_tab_fragment() throws Exception {
        String fragment = new ClassPathResource("templates/stats/fragments/tabs.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(fragment)
                .contains(
                        "th:fragment=\"tabs(activeTab)\"",
                        "th:href=\"@{/stats/question}\"",
                        ">질문</a>",
                        "th:href=\"@{/stats/ask-chat}\"",
                        ">Ask Chat</a>"
                );

        for (String template : List.of("daily", "weekly", "monthly", "type", "question", "question-overview", "withdrawal", "total", "ask-chat")) {
            String source = new ClassPathResource("templates/stats/" + template + ".html")
                    .getContentAsString(StandardCharsets.UTF_8);

            assertThat(source)
                    .as(template)
                    .contains("th:replace=\"~{stats/fragments/tabs :: tabs(${activeTab})}\"");
        }
    }

    @Test
    void typeStats_renders_recent_active_and_cumulative_charts_separately() throws Exception {
        when(typeStatsService.getTypeStats()).thenReturn(new TypeStatsViewModel(
                2L,
                List.of("취향", "감정"),
                List.of(2L, 1L),
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
                .andExpect(content().string(containsString("activeTypeByInterestChart")))
                .andExpect(content().string(containsString("cumulativeTypeByInterestChart")))
                .andExpect(content().string(containsString("최근 7일 · 관심사별 COMPLETED 생성 이력")))
                .andExpect(content().string(containsString("현재 활성 · COMPLETED · 삭제되지 않은 리포트")))
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

    @Test
    void periodStylesheet_is_served_as_static_resource() throws Exception {
        mockMvc.perform(get("/css/stats-period.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".period-picker-input")))
                .andExpect(content().string(containsString(".period-summary-grid")));
    }

    private PeakStatViewModel peak(long value, String periodLabel, boolean currentPeriod) {
        return new PeakStatViewModel(true, value, "2026-08-01", periodLabel, currentPeriod);
    }
}
