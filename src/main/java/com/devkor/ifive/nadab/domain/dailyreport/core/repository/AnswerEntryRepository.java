package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface AnswerEntryRepository extends JpaRepository<AnswerEntry, Long> {

    @Query("""
        select (count(a) > 0)
        from AnswerEntry a
        where a.user.id = :userId
          and a.question.id = :questionId
        """)
    boolean existsActiveAnswer(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );

    Optional<AnswerEntry> findByUserAndDate(User user, LocalDate date);

    boolean existsByUserAndCreatedAtBetween(User user, OffsetDateTime start, OffsetDateTime end);
}
