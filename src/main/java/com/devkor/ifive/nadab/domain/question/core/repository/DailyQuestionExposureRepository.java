package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionExposure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyQuestionExposureRepository extends JpaRepository<DailyQuestionExposure, Long> {

    Optional<DailyQuestionExposure> findTopByUserDailyQuestion_IdOrderBySequenceDesc(Long userDailyQuestionId);
}
