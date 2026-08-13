package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContentFactory;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(PeakStatsRepository.class)
class PeakStatsRepositoryTest extends PostgresIntegrationTestSupport {

    private static final LocalDate JANUARY_4 = LocalDate.of(2026, 1, 4);
    private static final LocalDate JANUARY_5 = LocalDate.of(2026, 1, 5);
    private static final LocalDate FEBRUARY_1 = LocalDate.of(2026, 2, 1);
    private static final LocalDate FUTURE_DATE = LocalDate.of(2100, 1, 4);

    @Autowired
    PeakStatsRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void findAllPeakStats_returns_empty_map_when_no_statistics_exist() {
        assertThat(repository.findAllPeakStats()).isEmpty();
    }

    @Test
    void findAllPeakStats_aggregates_all_metrics_and_uses_latest_period_for_ties() {
        User first = completedUser(OffsetDateTime.parse("2026-01-03T15:30:00Z"));
        User second = completedUser(OffsetDateTime.parse("2026-01-04T10:00:00+09:00"));
        User third = completedUser(OffsetDateTime.parse("2026-01-04T15:30:00Z"));
        User fourth = completedUser(OffsetDateTime.parse("2026-01-05T10:00:00+09:00"));
        incompleteUser(OffsetDateTime.parse("2026-01-05T11:00:00+09:00"));
        DailyQuestion question = em.getEntityManager().getReference(DailyQuestion.class, 1L);

        assignedQuestion(first, question, JANUARY_4);
        assignedQuestion(second, question, JANUARY_4);
        assignedQuestion(third, question, JANUARY_5);
        assignedQuestion(fourth, question, JANUARY_5);

        dailyReport(first, question, JANUARY_4, DailyReportStatus.COMPLETED);
        dailyReport(second, question, JANUARY_4, DailyReportStatus.COMPLETED);
        dailyReport(first, question, JANUARY_5, DailyReportStatus.COMPLETED);
        dailyReport(second, question, JANUARY_5, DailyReportStatus.COMPLETED);
        dailyReport(first, question, LocalDate.of(2026, 1, 6), DailyReportStatus.COMPLETED);
        dailyReport(first, question, FEBRUARY_1, DailyReportStatus.COMPLETED);
        dailyReport(second, question, FEBRUARY_1, DailyReportStatus.COMPLETED);
        dailyReport(third, question, LocalDate.of(2026, 2, 2), DailyReportStatus.FAILED);

        weeklyReport(first, JANUARY_4, WeeklyReportStatus.COMPLETED);
        weeklyReport(first, JANUARY_5, WeeklyReportStatus.COMPLETED);
        weeklyReport(second, JANUARY_5, WeeklyReportStatus.COMPLETED);
        weeklyReport(third, LocalDate.of(2026, 1, 6), WeeklyReportStatus.FAILED);

        monthlyReportV1(first, LocalDate.of(2026, 1, 15), MonthlyReportStatus.COMPLETED);
        monthlyReportV2(second, LocalDate.of(2026, 1, 20), MonthlyReportStatus.COMPLETED);
        monthlyReportV1(first, LocalDate.of(2026, 2, 5), MonthlyReportStatus.COMPLETED);
        monthlyReportV2(second, LocalDate.of(2026, 2, 6), MonthlyReportStatus.COMPLETED);
        monthlyReportV1(third, LocalDate.of(2026, 3, 5), MonthlyReportStatus.FAILED);
        createFutureStatistics(question);
        em.flush();
        em.clear();

        Map<PeakMetric, PeakStat> peaks = repository.findAllPeakStats();

        assertThat(peaks).hasSize(PeakMetric.values().length);
        assertThat(peaks)
                .containsEntry(PeakMetric.DAILY_SIGNUP, peak(2L, JANUARY_5))
                .containsEntry(PeakMetric.DAILY_ASSIGNED_QUESTION, peak(2L, JANUARY_5))
                .containsEntry(PeakMetric.DAU, peak(2L, FEBRUARY_1))
                .containsEntry(PeakMetric.WEEKLY_SIGNUP, peak(2L, JANUARY_5))
                .containsEntry(PeakMetric.WEEKLY_ASSIGNED_QUESTION, peak(2L, JANUARY_5))
                .containsEntry(PeakMetric.WEEKLY_DAILY_REPORT, peak(3L, JANUARY_5))
                .containsEntry(PeakMetric.WEEKLY_REPORT, peak(2L, JANUARY_5))
                .containsEntry(PeakMetric.WAU, peak(2L, LocalDate.of(2026, 1, 26)))
                .containsEntry(PeakMetric.MONTHLY_SIGNUP, peak(4L, LocalDate.of(2026, 1, 1)))
                .containsEntry(PeakMetric.MONTHLY_ASSIGNED_QUESTION, peak(4L, LocalDate.of(2026, 1, 1)))
                .containsEntry(PeakMetric.MONTHLY_DAILY_REPORT, peak(5L, LocalDate.of(2026, 1, 1)))
                .containsEntry(PeakMetric.MONTHLY_REPORT, peak(2L, LocalDate.of(2026, 2, 1)))
                .containsEntry(PeakMetric.MAU, peak(2L, LocalDate.of(2026, 2, 1)));
    }

    private void createFutureStatistics(DailyQuestion question) {
        User first = completedUser(OffsetDateTime.parse("2100-01-04T10:00:00+09:00"));
        User second = completedUser(OffsetDateTime.parse("2100-01-04T11:00:00+09:00"));
        User third = completedUser(OffsetDateTime.parse("2100-01-04T12:00:00+09:00"));

        for (User user : new User[]{first, second, third}) {
            assignedQuestion(user, question, FUTURE_DATE);
            dailyReport(user, question, FUTURE_DATE, DailyReportStatus.COMPLETED);
            weeklyReport(user, FUTURE_DATE, WeeklyReportStatus.COMPLETED);
        }
        monthlyReportV1(first, FUTURE_DATE, MonthlyReportStatus.COMPLETED);
        monthlyReportV1(second, FUTURE_DATE, MonthlyReportStatus.COMPLETED);
        monthlyReportV2(third, FUTURE_DATE, MonthlyReportStatus.COMPLETED);
    }

    private User completedUser(OffsetDateTime registeredAt) {
        User user = new UserBuilder(em).build();
        user.updateSignupStatus(SignupStatusType.COMPLETED);
        ReflectionTestUtils.setField(user, "registeredAt", registeredAt);
        return user;
    }

    private void incompleteUser(OffsetDateTime registeredAt) {
        User user = new UserBuilder(em).build();
        ReflectionTestUtils.setField(user, "registeredAt", registeredAt);
    }

    private void assignedQuestion(User user, DailyQuestion question, LocalDate date) {
        em.persist(UserDailyQuestion.create(user, date, question));
    }

    private void dailyReport(User user, DailyQuestion question, LocalDate date, DailyReportStatus status) {
        AnswerEntry answer = AnswerEntry.create(user, question, "answer", date, null);
        em.persist(answer);
        em.persist(DailyReport.create(answer, null, "report", date, status));
    }

    private void weeklyReport(User user, LocalDate date, WeeklyReportStatus status) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        em.persist(WeeklyReport.create(
                user,
                weekStart,
                weekStart.plusDays(6),
                ReportContentFactory.empty(),
                date,
                status
        ));
    }

    private void monthlyReportV1(User user, LocalDate date, MonthlyReportStatus status) {
        LocalDate targetMonth = date.minusMonths(1).withDayOfMonth(1);
        em.persist(MonthlyReport.create(
                user,
                targetMonth,
                targetMonth.withDayOfMonth(targetMonth.lengthOfMonth()),
                ReportContentFactory.empty(),
                date,
                status
        ));
    }

    private void monthlyReportV2(User user, LocalDate date, MonthlyReportStatus status) {
        LocalDate targetMonth = date.minusMonths(1).withDayOfMonth(1);
        em.persist(MonthlyReportV2.create(
                user,
                targetMonth,
                targetMonth.withDayOfMonth(targetMonth.lengthOfMonth()),
                MonthlyReportV2ContentFactory.empty(),
                date,
                status,
                MonthlyReportImageStatus.PENDING,
                MonthlyReportComparisonType.BASELINE
        ));
    }

    private PeakStat peak(long value, LocalDate periodStart) {
        return new PeakStat(value, periodStart);
    }
}
