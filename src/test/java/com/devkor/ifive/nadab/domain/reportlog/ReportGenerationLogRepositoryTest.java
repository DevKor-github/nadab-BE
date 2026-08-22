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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
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

    @Test
    void save_token_usage() {
        // given
        User user = new UserBuilder(em).build();
        ReportGenerationLog log = startLog(user, ReportGenerationType.DAILY, 101L, ReportGenerationStep.DAILY_GENERATE);
        log.recordTokenUsage(100L, 50L, 150L, 30L);
        ReportGenerationLog saved = reportGenerationLogRepository.save(log);

        em.flush();
        em.clear();

        // when
        ReportGenerationLog found = reportGenerationLogRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getInputTokens()).isEqualTo(100L);
        assertThat(found.getOutputTokens()).isEqualTo(50L);
        assertThat(found.getTotalTokens()).isEqualTo(150L);
        assertThat(found.getThinkingTokens()).isEqualTo(30L);
    }

    @Test
    void find_all_for_admin_filters_by_nickname_and_email() {
        // given
        User matchingUser = user("alice@example.com", "alice");
        User sameNicknameUser = user("bob@example.com", "alice-other");
        User otherUser = user("carol@example.com", "carol");

        ReportGenerationLog matchingLog = startLog(
                matchingUser,
                ReportGenerationType.MONTHLY_V2,
                301L,
                ReportGenerationStep.MONTHLY_V2_TEXT_CONFIRM
        );
        reportGenerationLogRepository.save(matchingLog);
        reportGenerationLogRepository.save(
                startLog(
                        sameNicknameUser,
                        ReportGenerationType.MONTHLY_V2,
                        302L,
                        ReportGenerationStep.MONTHLY_V2_TEXT_CONFIRM
                )
        );
        reportGenerationLogRepository.save(
                startLog(
                        otherUser,
                        ReportGenerationType.DAILY,
                        303L,
                        ReportGenerationStep.DAILY_GENERATE
                )
        );

        em.flush();
        em.clear();

        // when
        Page<ReportGenerationLog> logs = reportGenerationLogRepository.findAllForAdmin(
                "ali",
                "alice@example.com",
                PageRequest.of(0, 20)
        );

        // then
        assertThat(logs.getTotalElements()).isEqualTo(1);
        assertThat(logs.getContent()).extracting(ReportGenerationLog::getReportId)
                .containsExactly(301L);
        assertThat(logs.getContent().get(0).getUser().getEmail())
                .isEqualTo("alice@example.com");
    }

    @Test
    void find_all_for_admin_keeps_null_user_logs_and_orders_latest_first() {
        // given
        User user = user("latest@example.com", "latest");
        ReportGenerationLog first = reportGenerationLogRepository.save(
                startLog(user, ReportGenerationType.DAILY, 401L, ReportGenerationStep.DAILY_GENERATE)
        );
        ReportGenerationLog second = reportGenerationLogRepository.save(
                startLog(user, ReportGenerationType.WEEKLY, 402L, ReportGenerationStep.WEEKLY_GENERATE)
        );
        ReportGenerationLog orphan = reportGenerationLogRepository.save(
                startLog(null, ReportGenerationType.TYPE, 403L, ReportGenerationStep.TYPE_SELECTION)
        );

        em.flush();
        em.clear();

        // when
        Page<ReportGenerationLog> logs = reportGenerationLogRepository.findAllForAdmin(
                null,
                null,
                PageRequest.of(
                        0,
                        20,
                        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
                )
        );

        // then
        assertThat(logs.getTotalElements()).isEqualTo(3);
        assertThat(logs.getContent())
                .isSortedAccordingTo(
                        Comparator.comparing(ReportGenerationLog::getCreatedAt)
                                .thenComparing(ReportGenerationLog::getId)
                                .reversed()
                );
        assertThat(logs.getContent())
                .extracting(ReportGenerationLog::getId)
                .contains(first.getId(), second.getId(), orphan.getId());
        assertThat(logs.getContent())
                .filteredOn(log -> log.getId().equals(orphan.getId()))
                .singleElement()
                .satisfies(log -> assertThat(log.getUser()).isNull());
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

    private User user(String email, String nickname) {
        User user = User.createUser(email, "hashed_password");
        user.updateNickname(nickname);
        em.persist(user);
        return user;
    }
}
