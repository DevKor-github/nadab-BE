package com.devkor.ifive.nadab.domain.question.core.service;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposure;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposureSource;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionExposureRepository;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyQuestionExposureService {

    private final DailyQuestionExposureRepository exposureRepository;
    private final DailyQuestionRevisionRepository revisionRepository;

    public void recordInitialAssignment(UserDailyQuestion assignment) {
        DailyQuestionRevision revision = currentRevisionOf(assignment.getDailyQuestion());
        exposureRepository.save(DailyQuestionExposure.create(
                assignment,
                revision,
                assignment.getDate(),
                0,
                DailyQuestionExposureSource.INITIAL
        ));
    }

    public void recordReroll(UserDailyQuestion assignment, DailyQuestion newQuestion) {
        Optional<DailyQuestionExposure> latestExposure = latestExposureOf(assignment);
        int nextSequence = latestExposure.map(exposure -> exposure.getSequence() + 1).orElse(0);

        latestExposure.filter(DailyQuestionExposure::isOpen).ifPresent(exposure -> {
            exposure.markRerolled();
            exposureRepository.flush();
        });

        DailyQuestionRevision revision = currentRevisionOf(newQuestion);
        exposureRepository.save(DailyQuestionExposure.create(
                assignment,
                revision,
                assignment.getDate(),
                nextSequence,
                DailyQuestionExposureSource.REROLL
        ));
    }

    public Optional<DailyQuestionRevision> recordAnswer(UserDailyQuestion assignment) {
        return latestExposureOf(assignment)
                .filter(DailyQuestionExposure::isOpen)
                .map(exposure -> {
                    exposure.markAnswered();
                    return exposure.getDailyQuestionRevision();
                });
    }

    @Transactional(readOnly = true)
    public Optional<DailyQuestionRevision> findLatestRevision(UserDailyQuestion assignment) {
        return latestExposureOf(assignment).map(DailyQuestionExposure::getDailyQuestionRevision);
    }

    private Optional<DailyQuestionExposure> latestExposureOf(UserDailyQuestion assignment) {
        if (assignment.getId() == null) {
            return Optional.empty();
        }
        return exposureRepository.findTopByUserDailyQuestion_IdOrderBySequenceDesc(assignment.getId());
    }

    private DailyQuestionRevision currentRevisionOf(DailyQuestion question) {
        return revisionRepository
                .findByDailyQuestion_IdAndRevisionNo(question.getId(), question.getCurrentRevisionNo())
                .orElseThrow(() -> new IllegalStateException(
                        "Current daily question revision not found: questionId=%d, revisionNo=%d"
                                .formatted(question.getId(), question.getCurrentRevisionNo())
                ));
    }
}
