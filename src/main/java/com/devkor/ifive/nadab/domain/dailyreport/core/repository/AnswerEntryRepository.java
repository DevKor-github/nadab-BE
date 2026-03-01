package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.UserWithLastAnswerDate;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
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

    boolean existsByUserAndDate(User user, LocalDate date);

    /**
     * 특정 기간 내에 최소 N개의 답변을 작성한 사용자 조회
     */
    @Query("""
        select a.user
        from AnswerEntry a
        where a.date >= :startDate
          and a.date <= :endDate
          and a.user.deletedAt is null
        group by a.user
        having count(a) >= :minCount
        """)
    List<User> findUsersWithMinAnswers(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minCount") long minCount
    );

    /**
     * 특정 사용자의 특정 InterestCode 답변 개수 조회
     */
    @Query("""
        select count(a)
        from AnswerEntry a
        where a.user.id = :userId
          and a.user.deletedAt is null
          and a.question.interest.code = :interestCode
        """)
    long countByUserIdAndInterestCode(
            @Param("userId") Long userId,
            @Param("interestCode") InterestCode interestCode
    );

    /**
     * 마지막 답변일이 특정 일수 이전인 사용자 조회
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.UserWithLastAnswerDate(a.user, max(a.date))
        from AnswerEntry a
        where a.user.deletedAt is null
        group by a.user
        having max(a.date) <= :cutoffDate
        """)
    List<UserWithLastAnswerDate> findUsersWithLastAnswerBefore(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * 특정 날짜에 답변한 사용자 ID 목록 조회
     */
    @Query("""
        select a.user.id
        from AnswerEntry a
        where a.user.id in :userIds
          and a.date = :date
        """)
    List<Long> findUserIdsWithAnswerOnDate(
            @Param("userIds") List<Long> userIds,
            @Param("date") LocalDate date
    );
}
