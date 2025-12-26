package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Long> {

    @Query("""
        select q
        from DailyQuestion q
        where q.interest.id = :interestId
          and (:levelOnly is null or q.questionLevel = :levelOnly)
        order by function('random')
        """)
    Optional<DailyQuestion> findRandomByInterest(
            @Param("interestId") Integer interestId,
            @Param("levelOnly") Integer levelOnly
    );

    @Query("""
        select q
        from DailyQuestion q
        where q.interest.id = :interestId
          and q.id <> :excludeId
          and (:levelOnly is null or q.questionLevel = :levelOnly)
        order by function('random')
        """)
    Optional<DailyQuestion> findRandomByInterestExcluding(
            @Param("interestId") Integer interestId,
            @Param("excludeId") Long excludeId,
            @Param("levelOnly") Integer levelOnly
    );
}
