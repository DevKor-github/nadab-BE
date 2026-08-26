package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposure;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposureSource;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionRevisionStatsViewModel;
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
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@ActiveProfiles("test")
@Import(QuestionStatsRepository.class)
class QuestionStatsRepositoryTest extends PostgresIntegrationTestSupport {

    private static final LocalDate ASSIGNMENT_DATE = LocalDate.of(2026, 8, 26);

    @Autowired
    QuestionStatsRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void lists_questions_with_their_current_revision_state() {
        insertSecondRevision();
        em.flush();
        em.clear();

        DailyQuestionListItemViewModel question = repository.findQuestions().stream()
                .filter(item -> item.questionId() == 1L)
                .findFirst()
                .orElseThrow();

        assertThat(question.currentRevisionNo()).isEqualTo(2);
        assertThat(question.questionText()).isEqualTo("수정된 질문");
        assertThat(question.active()).isTrue();
    }

    @Test
    void aggregates_terminal_and_open_exposures_by_revision() {
        insertSecondRevision();
        em.flush();
        em.clear();

        DailyQuestion question = em.find(DailyQuestion.class, 1L);
        DailyQuestionRevision revision1 = findRevision(1);
        DailyQuestionRevision revision2 = findRevision(2);

        persistExposure(question, revision1, ExposureState.ANSWERED);
        persistExposure(question, revision1, ExposureState.REROLLED);
        persistExposure(question, revision1, ExposureState.OPEN);
        persistExposure(question, revision2, ExposureState.ANSWERED);
        persistExposure(question, revision2, ExposureState.OPEN);
        em.flush();
        em.clear();

        List<DailyQuestionRevisionStatsViewModel> stats = repository.findRevisionStats(1L);

        assertThat(stats).extracting(DailyQuestionRevisionStatsViewModel::revisionNo)
                .containsExactly(2, 1);

        DailyQuestionRevisionStatsViewModel revision2Stats = stats.get(0);
        assertThat(revision2Stats.questionText()).isEqualTo("수정된 질문");
        assertThat(revision2Stats.sourceMigration()).isEqualTo("V_TEST_2");
        assertThat(revision2Stats.exposureCount()).isEqualTo(2L);
        assertThat(revision2Stats.answeredCount()).isEqualTo(1L);
        assertThat(revision2Stats.rerolledCount()).isZero();
        assertThat(revision2Stats.unansweredCount()).isEqualTo(1L);
        assertThat(revision2Stats.answerRate()).isEqualTo(0.5);

        DailyQuestionRevisionStatsViewModel revision1Stats = stats.get(1);
        assertThat(revision1Stats.exposureCount()).isEqualTo(3L);
        assertThat(revision1Stats.answeredCount()).isEqualTo(1L);
        assertThat(revision1Stats.rerolledCount()).isEqualTo(1L);
        assertThat(revision1Stats.unansweredCount()).isEqualTo(1L);
        assertThat(revision1Stats.answerRate()).isCloseTo(1.0 / 3.0, within(0.0001));
    }

    @Test
    void includes_revision_without_exposure_as_zero_counts() {
        List<DailyQuestionRevisionStatsViewModel> stats = repository.findRevisionStats(2L);

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().exposureCount()).isZero();
        assertThat(stats.getFirst().answeredCount()).isZero();
        assertThat(stats.getFirst().rerolledCount()).isZero();
        assertThat(stats.getFirst().unansweredCount()).isZero();
        assertThat(stats.getFirst().answerRate()).isZero();
    }

    @Test
    void aggregates_current_revision_and_all_revision_stats_per_question() {
        insertSecondRevision();
        em.flush();
        em.clear();

        DailyQuestion question = em.find(DailyQuestion.class, 1L);
        DailyQuestionRevision revision1 = findRevision(1);
        DailyQuestionRevision revision2 = findRevision(2);

        persistExposure(question, revision1, ExposureState.ANSWERED);
        persistExposure(question, revision1, ExposureState.REROLLED);
        persistExposure(question, revision1, ExposureState.OPEN);
        persistExposure(question, revision2, ExposureState.ANSWERED);
        persistExposure(question, revision2, ExposureState.OPEN);
        em.flush();
        em.clear();

        List<DailyQuestionOverviewRowViewModel> overview = repository.findQuestionOverview();

        DailyQuestionOverviewRowViewModel question1 = overview.stream()
                .filter(row -> row.questionId() == 1L)
                .findFirst()
                .orElseThrow();
        assertThat(question1.questionText()).isEqualTo("수정된 질문");
        assertThat(question1.currentRevisionNo()).isEqualTo(2);
        assertThat(question1.currentRevisionEffectiveFrom())
                .isEqualTo(OffsetDateTime.parse("2026-08-26T00:00:00+09:00"));
        assertThat(question1.currentExposureCount()).isEqualTo(2L);
        assertThat(question1.currentAnsweredCount()).isEqualTo(1L);
        assertThat(question1.currentRerolledCount()).isZero();
        assertThat(question1.currentUnansweredCount()).isEqualTo(1L);
        assertThat(question1.currentAnswerRate()).isEqualTo(0.5);
        assertThat(question1.currentRerollRate()).isZero();
        assertThat(question1.totalExposureCount()).isEqualTo(5L);
        assertThat(question1.totalAnsweredCount()).isEqualTo(2L);
        assertThat(question1.totalRerolledCount()).isEqualTo(1L);
        assertThat(question1.totalUnansweredCount()).isEqualTo(2L);
        assertThat(question1.totalAnswerRate()).isEqualTo(0.4);

        DailyQuestionOverviewRowViewModel question2 = overview.stream()
                .filter(row -> row.questionId() == 2L)
                .findFirst()
                .orElseThrow();
        assertThat(question2.currentExposureCount()).isZero();
        assertThat(question2.totalExposureCount()).isZero();
        assertThat(question2.currentAnswerRate()).isZero();
    }

    private void insertSecondRevision() {
        em.getEntityManager().createNativeQuery("""
                UPDATE daily_questions
                SET current_revision_no = 2,
                    question_text = '수정된 질문'
                WHERE id = 1
                """).executeUpdate();
        em.getEntityManager().createNativeQuery("""
                INSERT INTO daily_question_revisions (
                    daily_question_id,
                    revision_no,
                    interest_id,
                    question_text,
                    question_level,
                    empathy_guide,
                    hint_guide,
                    leading_question_guide,
                    deleted_at,
                    effective_from,
                    source_migration
                )
                SELECT
                    id,
                    2,
                    interest_id,
                    question_text,
                    question_level,
                    empathy_guide,
                    hint_guide,
                    leading_question_guide,
                    deleted_at,
                    TIMESTAMPTZ '2026-08-26 00:00:00+09',
                    'V_TEST_2'
                FROM daily_questions
                WHERE id = 1
                """).executeUpdate();
    }

    private DailyQuestionRevision findRevision(int revisionNo) {
        return em.getEntityManager().createQuery("""
                        select revision
                        from DailyQuestionRevision revision
                        where revision.dailyQuestion.id = 1
                          and revision.revisionNo = :revisionNo
                        """, DailyQuestionRevision.class)
                .setParameter("revisionNo", revisionNo)
                .getSingleResult();
    }

    private void persistExposure(
            DailyQuestion question,
            DailyQuestionRevision revision,
            ExposureState state
    ) {
        User user = new UserBuilder(em).build();
        UserDailyQuestion assignment = UserDailyQuestion.create(user, ASSIGNMENT_DATE, question);
        em.persist(assignment);

        DailyQuestionExposure exposure = DailyQuestionExposure.create(
                assignment,
                revision,
                ASSIGNMENT_DATE,
                0,
                DailyQuestionExposureSource.INITIAL
        );
        if (state == ExposureState.ANSWERED) {
            exposure.markAnswered();
        } else if (state == ExposureState.REROLLED) {
            exposure.markRerolled();
        }
        em.persist(exposure);
    }

    private enum ExposureState {
        OPEN,
        ANSWERED,
        REROLLED
    }
}
