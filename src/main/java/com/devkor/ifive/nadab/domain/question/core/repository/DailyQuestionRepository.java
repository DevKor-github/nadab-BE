package com.devkor.ifive.nadab.domain.question.core.repository;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Long> {

    /**
     * 랜덤으로 질문 하나를 조회합니다. 단, 사용자가 이미 답변한 질문은 제외합니다.
     * @param levelOnly: null이면 레벨 무관, 아니면 해당 레벨만
     * @param pageable : 페이지 정보 (1개만 조회하기 위해 사용)
     * @return
     */
    @Query("""
    select q
    from DailyQuestion q
    where q.interest.id = :interestId
      and (:levelOnly is null or q.questionLevel = :levelOnly)
      and q.deletedAt is null
      and not exists (
          select 1
          from AnswerEntry a
          where a.user.id = :userId
            and a.question.id = q.id
      )
    order by function('random')
    """)
    List<DailyQuestion> findRandomByInterestExcludingAnswered(
            @Param("userId") Long userId,
            @Param("interestId") Long interestId,
            @Param("levelOnly") Integer levelOnly,
            Pageable pageable
    );

    /**
     * 이미 뽑힌 오늘의 질문을 제외하고 랜덤으로 질문 하나를 조회합니다.
     * @param interestId
     * @param excludeId: 제외할 오늘의 질문 아이디
     * @param levelOnly: null이면 레벨 무관, 아니면 해당 레벨만
     * @param pageable : 페이지 정보 (1개만 조회하기 위해 사용)
     * @return
     */
    @Query("""
    select q
    from DailyQuestion q
    where q.interest.id = :interestId
      and q.id <> :excludeId
      and (:levelOnly is null or q.questionLevel = :levelOnly)
      and q.deletedAt is null
      and not exists (
          select 1
          from AnswerEntry a
          where a.user.id = :userId
            and a.question.id = q.id
      )
    order by function('random')
    """)
    List<DailyQuestion> findRandomByInterestExcludingAnsweredAndExcludingId(
            @Param("userId") Long userId,
            @Param("interestId") Long interestId,
            @Param("excludeId") Long excludeId,
            @Param("levelOnly") Integer levelOnly,
            Pageable pageable
    );
}
