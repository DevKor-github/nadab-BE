package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Long> {

    /**
     * 랜덤으로 질문 하나를 조회합니다.
     * @param interestId
     * @param levelOnly: null이면 레벨 무관, 아니면 해당 레벨만
     * @return
     */
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

    /**
     * 특정 질문을 제외하고 랜덤으로 질문 하나를 조회합니다.
     * @param interestId
     * @param excludeId
     * @param levelOnly: null이면 레벨 무관, 아니면 해당 레벨만
     * @return
     */
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
