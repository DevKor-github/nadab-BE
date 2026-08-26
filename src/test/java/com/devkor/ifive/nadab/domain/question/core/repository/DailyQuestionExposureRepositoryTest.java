package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposure;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposureSource;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class DailyQuestionExposureRepositoryTest extends PostgresIntegrationTestSupport {

    private static final LocalDate ASSIGNMENT_DATE = LocalDate.of(2026, 8, 25);

    @Autowired
    DailyQuestionExposureRepository exposureRepository;

    @Autowired
    DailyQuestionRevisionRepository revisionRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void supports_only_post_baseline_exposure_sources() {
        assertThat(DailyQuestionExposureSource.values())
                .containsExactly(
                        DailyQuestionExposureSource.INITIAL,
                        DailyQuestionExposureSource.REROLL
                );
        assertThat(exposureRepository.count()).isZero();
    }

    @Test
    void persists_exposure_for_exact_question_revision() {
        UserDailyQuestion assignment = assignment();
        DailyQuestionRevision revision = currentRevision(assignment.getDailyQuestion());

        DailyQuestionExposure exposure = exposureRepository.saveAndFlush(DailyQuestionExposure.create(
                assignment,
                revision,
                ASSIGNMENT_DATE,
                0,
                DailyQuestionExposureSource.INITIAL
        ));
        em.clear();

        DailyQuestionExposure found = exposureRepository.findById(exposure.getId()).orElseThrow();
        assertThat(found.getUserDailyQuestion().getId()).isEqualTo(assignment.getId());
        assertThat(found.getDailyQuestionRevision().getId()).isEqualTo(revision.getId());
        assertThat(found.getAssignmentDate()).isEqualTo(ASSIGNMENT_DATE);
        assertThat(found.getSequence()).isZero();
        assertThat(found.getSource()).isEqualTo(DailyQuestionExposureSource.INITIAL);
        assertThat(found.getAssignedAt()).isNotNull();
        assertThat(found.getRerolledAt()).isNull();
        assertThat(found.getAnsweredAt()).isNull();
    }

    @Test
    void rejects_two_open_exposures_for_same_assignment() {
        UserDailyQuestion assignment = assignment();
        DailyQuestionRevision revision = currentRevision(assignment.getDailyQuestion());
        exposureRepository.saveAndFlush(DailyQuestionExposure.create(
                assignment,
                revision,
                ASSIGNMENT_DATE,
                0,
                DailyQuestionExposureSource.INITIAL
        ));

        DailyQuestionExposure duplicateOpenExposure = DailyQuestionExposure.create(
                assignment,
                revision,
                ASSIGNMENT_DATE,
                1,
                DailyQuestionExposureSource.REROLL
        );

        assertThatThrownBy(() -> exposureRepository.saveAndFlush(duplicateOpenExposure))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejects_exposure_marked_as_both_rerolled_and_answered() {
        UserDailyQuestion assignment = assignment();
        DailyQuestionExposure exposure = DailyQuestionExposure.create(
                assignment,
                currentRevision(assignment.getDailyQuestion()),
                ASSIGNMENT_DATE,
                0,
                DailyQuestionExposureSource.INITIAL
        );
        OffsetDateTime terminalAt = exposure.getAssignedAt().plusMinutes(1);
        ReflectionTestUtils.setField(exposure, "rerolledAt", terminalAt);
        ReflectionTestUtils.setField(exposure, "answeredAt", terminalAt);

        assertThatThrownBy(() -> exposureRepository.saveAndFlush(exposure))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void keeps_anonymized_exposure_when_user_is_hard_deleted() {
        UserDailyQuestion assignment = assignment();
        Long userId = assignment.getUser().getId();
        DailyQuestionExposure exposure = exposureRepository.saveAndFlush(DailyQuestionExposure.create(
                assignment,
                currentRevision(assignment.getDailyQuestion()),
                ASSIGNMENT_DATE,
                0,
                DailyQuestionExposureSource.INITIAL
        ));

        em.getEntityManager().createNativeQuery("DELETE FROM users WHERE id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        em.flush();
        em.clear();

        DailyQuestionExposure found = exposureRepository.findById(exposure.getId()).orElseThrow();
        assertThat(found.getUserDailyQuestion()).isNull();
        assertThat(found.getDailyQuestionRevision()).isNotNull();
    }

    private UserDailyQuestion assignment() {
        User user = new UserBuilder(em).build();
        DailyQuestion question = em.find(DailyQuestion.class, 1L);
        UserDailyQuestion assignment = UserDailyQuestion.create(user, ASSIGNMENT_DATE, question);
        em.persistAndFlush(assignment);
        return assignment;
    }

    private DailyQuestionRevision currentRevision(DailyQuestion question) {
        return revisionRepository
                .findByDailyQuestion_IdAndRevisionNo(question.getId(), question.getCurrentRevisionNo())
                .orElseThrow();
    }
}
