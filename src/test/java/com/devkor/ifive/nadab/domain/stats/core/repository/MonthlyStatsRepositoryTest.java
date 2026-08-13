package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContentFactory;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(MonthlyStatsRepository.class)
class MonthlyStatsRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void counts_completed_and_in_progress_monthly_reports_by_version() {
        LocalDate reportDate = LocalDate.of(2026, 8, 1);

        em.persist(v1Report(new UserBuilder(em).build(), reportDate, MonthlyReportStatus.COMPLETED));
        em.persist(v1Report(new UserBuilder(em).build(), reportDate, MonthlyReportStatus.IN_PROGRESS));
        em.persist(v2Report(new UserBuilder(em).build(), reportDate, MonthlyReportStatus.COMPLETED));
        em.persist(v2Report(new UserBuilder(em).build(), reportDate, MonthlyReportStatus.IN_PROGRESS));
        em.flush();
        em.clear();

        List<DateCountDto> completedV1 = monthlyStatsRepository
                .findCompletedMonthlyReportV1CountsByDateBetween(reportDate, reportDate);
        List<DateCountDto> completedV2 = monthlyStatsRepository
                .findCompletedMonthlyReportV2CountsByDateBetween(reportDate, reportDate);

        assertThat(completedV1).containsExactly(new DateCountDto(reportDate, 1L));
        assertThat(completedV2).containsExactly(new DateCountDto(reportDate, 1L));
        assertThat(monthlyStatsRepository.countInProgressMonthlyReportV1Now()).isEqualTo(1L);
        assertThat(monthlyStatsRepository.countInProgressMonthlyReportV2Now()).isEqualTo(1L);
    }

    private MonthlyReport v1Report(User user, LocalDate reportDate, MonthlyReportStatus status) {
        LocalDate monthStartDate = reportDate.minusMonths(1).withDayOfMonth(1);
        return MonthlyReport.create(
                user,
                monthStartDate,
                monthStartDate.withDayOfMonth(monthStartDate.lengthOfMonth()),
                ReportContentFactory.empty(),
                reportDate,
                status
        );
    }

    private MonthlyReportV2 v2Report(User user, LocalDate reportDate, MonthlyReportStatus status) {
        LocalDate monthStartDate = reportDate.minusMonths(1).withDayOfMonth(1);
        return MonthlyReportV2.create(
                user,
                monthStartDate,
                monthStartDate.withDayOfMonth(monthStartDate.lengthOfMonth()),
                MonthlyReportV2ContentFactory.empty(),
                reportDate,
                status,
                MonthlyReportImageStatus.PENDING,
                MonthlyReportComparisonType.BASELINE
        );
    }
}
