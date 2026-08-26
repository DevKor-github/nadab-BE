package com.devkor.ifive.nadab.domain.question.core.service;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposure;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposureSource;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionExposureRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(DailyQuestionExposureService.class)
class DailyQuestionExposureServiceTest extends PostgresIntegrationTestSupport {

    private static final LocalDate ASSIGNMENT_DATE = LocalDate.of(2026, 8, 26);

    @Autowired
    DailyQuestionExposureService exposureService;

    @Autowired
    DailyQuestionExposureRepository exposureRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void records_initial_assignment_with_current_revision() {
        UserDailyQuestion assignment = assignment(1L);

        exposureService.recordInitialAssignment(assignment);
        em.flush();
        em.clear();

        DailyQuestionExposure exposure = exposureRepository.findAll().getFirst();
        assertThat(exposure.getUserDailyQuestion().getId()).isEqualTo(assignment.getId());
        assertThat(exposure.getDailyQuestionRevision().getDailyQuestion().getId()).isEqualTo(1L);
        assertThat(exposure.getDailyQuestionRevision().getRevisionNo()).isEqualTo(1);
        assertThat(exposure.getSequence()).isZero();
        assertThat(exposure.getSource()).isEqualTo(DailyQuestionExposureSource.INITIAL);
        assertThat(exposure.isOpen()).isTrue();
    }

    @Test
    void reroll_closes_open_exposure_and_creates_next_revision_exposure() {
        UserDailyQuestion assignment = assignment(1L);
        exposureService.recordInitialAssignment(assignment);
        DailyQuestion newQuestion = em.find(DailyQuestion.class, 2L);

        assignment.rerollTo(newQuestion);
        exposureService.recordReroll(assignment, newQuestion);
        em.flush();
        em.clear();

        List<DailyQuestionExposure> exposures = exposuresBySequence();
        assertThat(exposures).hasSize(2);
        assertThat(exposures.get(0).getRerolledAt()).isNotNull();
        assertThat(exposures.get(0).isOpen()).isFalse();
        assertThat(exposures.get(1).getSequence()).isEqualTo(1);
        assertThat(exposures.get(1).getSource()).isEqualTo(DailyQuestionExposureSource.REROLL);
        assertThat(exposures.get(1).getDailyQuestionRevision().getDailyQuestion().getId()).isEqualTo(2L);
        assertThat(exposures.get(1).isOpen()).isTrue();
    }

    @Test
    void answer_closes_open_exposure_and_returns_its_revision() {
        UserDailyQuestion assignment = assignment(1L);
        exposureService.recordInitialAssignment(assignment);

        Optional<DailyQuestionRevision> revision = exposureService.recordAnswer(assignment);
        em.flush();
        em.clear();

        DailyQuestionExposure exposure = exposureRepository.findAll().getFirst();
        assertThat(revision).isPresent();
        assertThat(revision.orElseThrow().getId()).isEqualTo(exposure.getDailyQuestionRevision().getId());
        assertThat(exposure.getAnsweredAt()).isNotNull();
        assertThat(exposure.isOpen()).isFalse();
    }

    @Test
    void legacy_assignment_answer_does_not_create_or_adopt_exposure() {
        UserDailyQuestion assignment = assignment(1L);

        Optional<DailyQuestionRevision> revision = exposureService.recordAnswer(assignment);

        assertThat(revision).isEmpty();
        assertThat(exposureRepository.count()).isZero();
    }

    @Test
    void legacy_assignment_reroll_starts_tracking_from_reroll_event() {
        UserDailyQuestion assignment = assignment(1L);
        DailyQuestion newQuestion = em.find(DailyQuestion.class, 2L);

        assignment.rerollTo(newQuestion);
        exposureService.recordReroll(assignment, newQuestion);
        em.flush();
        em.clear();

        DailyQuestionExposure exposure = exposureRepository.findAll().getFirst();
        assertThat(exposure.getSequence()).isZero();
        assertThat(exposure.getSource()).isEqualTo(DailyQuestionExposureSource.REROLL);
        assertThat(exposure.getDailyQuestionRevision().getDailyQuestion().getId()).isEqualTo(2L);
        assertThat(exposure.isOpen()).isTrue();
    }

    private UserDailyQuestion assignment(Long questionId) {
        User user = new UserBuilder(em).build();
        DailyQuestion question = em.find(DailyQuestion.class, questionId);
        UserDailyQuestion assignment = UserDailyQuestion.create(user, ASSIGNMENT_DATE, question);
        em.persistAndFlush(assignment);
        return assignment;
    }

    private List<DailyQuestionExposure> exposuresBySequence() {
        return exposureRepository.findAll().stream()
                .sorted(Comparator.comparingInt(DailyQuestionExposure::getSequence))
                .toList();
    }
}
