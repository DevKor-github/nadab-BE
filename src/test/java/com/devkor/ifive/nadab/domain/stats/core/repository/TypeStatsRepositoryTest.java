package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportDateInterestCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestCountDto;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TypeStatsRepository.class)
class TypeStatsRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    TypeStatsRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void counts_active_and_historical_completed_type_reports_separately() {
        LocalDate startDate = LocalDate.of(2026, 8, 14);
        LocalDate endDate = LocalDate.of(2026, 8, 20);

        persistReport(InterestCode.PREFERENCE, endDate, TypeReportStatus.COMPLETED, false);
        persistReport(InterestCode.PREFERENCE, endDate.minusDays(1), TypeReportStatus.COMPLETED, true);
        persistReport(InterestCode.EMOTION, endDate.minusDays(1), TypeReportStatus.COMPLETED, false);
        persistReport(InterestCode.PREFERENCE, endDate, TypeReportStatus.PENDING, false);
        persistReport(InterestCode.EMOTION, endDate, TypeReportStatus.FAILED, false);
        persistReport(InterestCode.VALUES, startDate.minusDays(1), TypeReportStatus.COMPLETED, false);
        persistReport(InterestCode.ROUTINE, endDate, TypeReportStatus.IN_PROGRESS, false);
        persistReport(InterestCode.LOVE, endDate, TypeReportStatus.IN_PROGRESS, true);
        em.flush();
        em.clear();

        List<TypeReportInterestCountDto> activeTotals =
                repository.countActiveCompletedTypeReportsByInterest();
        List<TypeReportInterestCountDto> cumulativeTotals =
                repository.countCompletedTypeReportHistoryByInterest();
        List<TypeReportDateInterestCountDto> dailyCounts =
                repository.countCompletedTypeReportsByDateAndInterest(startDate, endDate);

        assertThat(activeTotals).containsExactlyInAnyOrder(
                new TypeReportInterestCountDto(InterestCode.PREFERENCE, 1L),
                new TypeReportInterestCountDto(InterestCode.EMOTION, 1L),
                new TypeReportInterestCountDto(InterestCode.VALUES, 1L)
        );
        assertThat(cumulativeTotals).containsExactlyInAnyOrder(
                new TypeReportInterestCountDto(InterestCode.PREFERENCE, 2L),
                new TypeReportInterestCountDto(InterestCode.EMOTION, 1L),
                new TypeReportInterestCountDto(InterestCode.VALUES, 1L)
        );
        assertThat(dailyCounts).containsExactlyInAnyOrder(
                new TypeReportDateInterestCountDto(endDate, InterestCode.PREFERENCE, 1L),
                new TypeReportDateInterestCountDto(endDate.minusDays(1), InterestCode.PREFERENCE, 1L),
                new TypeReportDateInterestCountDto(endDate.minusDays(1), InterestCode.EMOTION, 1L)
        );
        assertThat(repository.countInProgressTypeReportsNow()).isEqualTo(1L);
    }

    private void persistReport(
            InterestCode interestCode,
            LocalDate date,
            TypeReportStatus status,
            boolean deleted
    ) {
        User user = new UserBuilder(em).build();
        TypeReport report = TypeReport.create(
                user,
                null,
                interestCode,
                "분석 내용",
                "페르소나 1",
                "내용 1",
                "페르소나 2",
                "내용 2",
                date,
                status
        );
        if (deleted) {
            report.softDelete();
        }
        em.persist(report);
    }
}
