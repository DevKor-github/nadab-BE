package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(DailyStatsRepository.class)
class DailyStatsRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    DailyStatsRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void counts_shared_completed_daily_reports_for_requested_date() {
        LocalDate selectedDate = LocalDate.of(2026, 8, 13);

        persistReport(selectedDate, DailyReportStatus.COMPLETED, true);
        persistReport(selectedDate, DailyReportStatus.COMPLETED, false);
        persistReport(selectedDate, DailyReportStatus.FAILED, true);
        persistReport(selectedDate.minusDays(1), DailyReportStatus.COMPLETED, true);
        em.flush();
        em.clear();

        assertThat(repository.countSharedDailyReports(selectedDate)).isEqualTo(1L);
        assertThat(repository.countSharedDailyReports(selectedDate.minusDays(1))).isEqualTo(1L);
    }

    private void persistReport(LocalDate date, DailyReportStatus status, boolean shared) {
        User user = new UserBuilder(em).build();
        DailyQuestion question = em.getEntityManager().getReference(DailyQuestion.class, 1L);
        AnswerEntry answer = AnswerEntry.create(user, question, "답변", date, null);
        em.persist(answer);

        DailyReport report = DailyReport.create(answer, null, "리포트", date, status);
        if (shared) {
            report.startSharing();
        }
        em.persist(report);
    }
}
