package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyQuestionRevisionRepository extends JpaRepository<DailyQuestionRevision, Long> {

    Optional<DailyQuestionRevision> findByDailyQuestion_IdAndRevisionNo(Long dailyQuestionId, int revisionNo);
}
