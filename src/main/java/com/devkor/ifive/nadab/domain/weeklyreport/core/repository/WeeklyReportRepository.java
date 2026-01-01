package com.devkor.ifive.nadab.domain.weeklyreport.core.repository;


import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    /**
     * 저번 주(weekStartDate 기준) WeeklyReport 조회
     * - UniqueConstraint(user_id, week_start_date)를 활용해 weekStartDate로 찾음
     */
    Optional<WeeklyReport> findByUserAndWeekStartDate(User user, LocalDate weekStartDate);

    /**
     * PENDING -> COMPLETED 확정
     * - 분석 결과(discovered/good/improve) 저장
     * - analyzedAt 기록
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE WeeklyReport wr
           SET wr.status = :status,
               wr.discovered = :discovered,
               wr.good = :good,
               wr.improve = :improve,
               wr.analyzedAt = CURRENT_TIMESTAMP
         WHERE wr.id = :reportId
    """)
    int markCompleted(
            @Param("reportId") Long reportId,
            @Param("status") WeeklyReportStatus status,
            @Param("discovered") String discovered,
            @Param("good") String good,
            @Param("improve") String improve
    );

    /**
     * PENDING -> FAILED 확정
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE WeeklyReport wr
           SET wr.status = :status,
               wr.analyzedAt = CURRENT_TIMESTAMP
         WHERE wr.id = :reportId
    """)
    int markFailed(
            @Param("reportId") Long reportId,
            @Param("status") WeeklyReportStatus status
    );

    /**
     * 저번 주 레코드를 조회하기 위한 범위 조회 쿼리
     */
    @Query("""
        SELECT wr
          FROM WeeklyReport wr
         WHERE wr.user = :user
           AND wr.weekStartDate = :weekStartDate
           AND wr.weekEndDate = :weekEndDate
    """)
    Optional<WeeklyReport> findLastWeek(
            @Param("user") User user,
            @Param("weekStartDate") LocalDate weekStartDate,
            @Param("weekEndDate") LocalDate weekEndDate
    );
}