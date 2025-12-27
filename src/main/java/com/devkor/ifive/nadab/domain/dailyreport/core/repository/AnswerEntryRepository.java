package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerEntryRepository extends JpaRepository<AnswerEntry, Long> {

    @Query("""
        select (count(a) > 0)
        from AnswerEntry a
        where a.user.id = :userId
          and a.question.id = :questionId
          and a.deletedAt is null
        """)
    boolean existsActiveAnswer(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );
}
