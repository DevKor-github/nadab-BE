package com.devkor.ifive.nadab.domain.reportlog;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLogStatus;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.reportlog.core.repository.ReportGenerationLogRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReportGenerationLogRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    ReportGenerationLogRepository reportGenerationLogRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void find_by_report_type_and_report_id() {
        // given
        User user = new UserBuilder(em).build();
        ReportGenerationLog target = reportGenerationLogRepository.save(
                startLog(user, ReportGenerationType.DAILY, 101L, ReportGenerationStep.DAILY_GENERATE)
        );
        reportGenerationLogRepository.save(
                startLog(user, ReportGenerationType.WEEKLY, 202L, ReportGenerationStep.WEEKLY_GENERATE)
        );

        em.flush();
        em.clear();

        // when
        List<ReportGenerationLog> logs = reportGenerationLogRepository
                .findAllByReportTypeAndReportIdOrderByCreatedAtDesc(ReportGenerationType.DAILY, 101L);

        // then
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getId()).isEqualTo(target.getId());
        assertThat(logs.get(0).getStatus()).isEqualTo(ReportGenerationLogStatus.STARTED);
    }

    @Test
    void find_by_status() {
        // given
        User user = new UserBuilder(em).build();
        ReportGenerationLog succeeded = startLog(user, ReportGenerationType.DAILY, 101L, ReportGenerationStep.DAILY_GENERATE);
        succeeded.succeed();
        reportGenerationLogRepository.save(succeeded);

        ReportGenerationLog failed = startLog(user, ReportGenerationType.WEEKLY, 202L, ReportGenerationStep.WEEKLY_GENERATE);
        failed.fail("AI_NO_RESPONSE", "test.Exception", 503, "HTTP_503");
        reportGenerationLogRepository.save(failed);

        em.flush();
        em.clear();

        // when
        List<ReportGenerationLog> logs = reportGenerationLogRepository
                .findAllByStatusOrderByCreatedAtDesc(ReportGenerationLogStatus.FAILED);

        // then
        assertThat(logs)
                .extracting(ReportGenerationLog::getReportId)
                .contains(202L);
        assertThat(logs)
                .extracting(ReportGenerationLog::getStatus)
                .containsOnly(ReportGenerationLogStatus.FAILED);
    }

    private ReportGenerationLog startLog(
            User user,
            ReportGenerationType reportType,
            Long reportId,
            ReportGenerationStep step
    ) {
        return ReportGenerationLog.start(
                user,
                reportType,
                reportId,
                step,
                LlmProvider.OPENAI,
                "GPT_4_O_MINI"
        );
    }
}
