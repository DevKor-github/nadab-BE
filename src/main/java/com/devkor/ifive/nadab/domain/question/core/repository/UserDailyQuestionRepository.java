package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyQuestionRepository extends JpaRepository<UserDailyQuestion, Long> {

    Optional<UserDailyQuestion> findByUserIdAndDate(Long userId, LocalDate date);
}
