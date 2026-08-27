package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class DailyQuestionRevisionRepositoryTest extends PostgresIntegrationTestSupport {

    private static final String BASELINE_MIGRATION = "V20260825_1200";

    @Autowired
    DailyQuestionRevisionRepository revisionRepository;

    @Autowired
    AnswerEntryRepository answerEntryRepository;

    @Autowired
    TestEntityManager em;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void baseline_revision_matches_every_daily_question() {
        Long questionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM daily_questions",
                Long.class
        );
        Long baselineCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM daily_question_revisions WHERE source_migration = ?",
                Long.class,
                BASELINE_MIGRATION
        );
        Long mismatchCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM daily_questions q
                LEFT JOIN daily_question_revisions r
                  ON r.daily_question_id = q.id
                 AND r.revision_no = q.current_revision_no
                WHERE r.id IS NULL
                   OR r.interest_id IS DISTINCT FROM q.interest_id
                   OR r.question_text IS DISTINCT FROM q.question_text
                   OR r.question_level IS DISTINCT FROM q.question_level
                   OR r.empathy_guide IS DISTINCT FROM q.empathy_guide
                   OR r.hint_guide IS DISTINCT FROM q.hint_guide
                   OR r.leading_question_guide IS DISTINCT FROM q.leading_question_guide
                   OR r.deleted_at IS DISTINCT FROM q.deleted_at
                """, Long.class);

        assertThat(questionCount).isPositive();
        assertThat(baselineCount).isEqualTo(questionCount);
        assertThat(mismatchCount).isZero();
    }

    @Test
    void finds_revision_by_logical_question_id_and_revision_number() {
        DailyQuestion question = em.find(DailyQuestion.class, 1L);

        DailyQuestionRevision revision = revisionRepository
                .findByDailyQuestion_IdAndRevisionNo(question.getId(), question.getCurrentRevisionNo())
                .orElseThrow();

        assertThat(revision.getDailyQuestion().getId()).isEqualTo(question.getId());
        assertThat(revision.getRevisionNo()).isEqualTo(1);
        assertThat(revision.getQuestionText()).isEqualTo(question.getQuestionText());
        assertThat(revision.getQuestionLevel()).isEqualTo(question.getQuestionLevel());
        assertThat(revision.getSourceMigration()).isEqualTo(BASELINE_MIGRATION);
        assertThat(revision.getEffectiveFrom()).isNotNull();
    }

    @Test
    void legacy_answer_entry_can_remain_without_revision() {
        User user = new UserBuilder(em).build();
        DailyQuestion question = em.find(DailyQuestion.class, 1L);
        AnswerEntry answerEntry = AnswerEntry.create(
                user,
                question,
                "legacy answer",
                LocalDate.of(2026, 8, 24),
                null
        );

        em.persistAndFlush(answerEntry);
        em.clear();

        AnswerEntry found = em.find(AnswerEntry.class, answerEntry.getId());
        assertThat(found.getQuestionRevision()).isNull();
    }

    @Test
    void answer_entry_persists_matching_question_revision() {
        User user = new UserBuilder(em).build();
        DailyQuestion question = em.find(DailyQuestion.class, 1L);
        DailyQuestionRevision revision = revisionRepository
                .findByDailyQuestion_IdAndRevisionNo(question.getId(), question.getCurrentRevisionNo())
                .orElseThrow();
        AnswerEntry answerEntry = AnswerEntry.create(
                user,
                question,
                revision,
                "answer",
                LocalDate.of(2026, 8, 26),
                null
        );

        em.persistAndFlush(answerEntry);
        em.clear();

        AnswerEntry found = em.find(AnswerEntry.class, answerEntry.getId());
        assertThat(found.getQuestionRevision().getId()).isEqualTo(revision.getId());
    }

    @Test
    void rejects_answer_entry_revision_from_different_logical_question() {
        User user = new UserBuilder(em).build();
        DailyQuestion answerQuestion = em.find(DailyQuestion.class, 2L);
        DailyQuestionRevision differentQuestionRevision = revisionRepository
                .findByDailyQuestion_IdAndRevisionNo(1L, 1)
                .orElseThrow();
        AnswerEntry answerEntry = AnswerEntry.create(
                user,
                answerQuestion,
                differentQuestionRevision,
                "answer",
                LocalDate.of(2026, 8, 25),
                null
        );

        assertThatThrownBy(() -> answerEntryRepository.saveAndFlush(answerEntry))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
