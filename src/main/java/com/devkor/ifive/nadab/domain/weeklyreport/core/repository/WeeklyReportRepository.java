package com.devkor.ifive.nadab.domain.weeklyreport.core.repository;


import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    /**
     * 저번 주(weekStartDate 기준) WeeklyReport 조회
     * - UniqueConstraint(user_id, week_start_date)를 활용해 weekStartDate로 찾음
     */
    Optional<WeeklyReport> findByUserAndWeekStartDate(User user, LocalDate weekStartDate);

    Optional<WeeklyReport> findByUserIdAndWeekStartDateAndStatus(
            Long userId,
            LocalDate weekStartDate,
            WeeklyReportStatus status
    );

    // 특정 시점(snapshotDate) 이전에 완료된(COMPLETED) 주간 리포트 조회
    List<WeeklyReport> findAllByUserIdAndStatusAndWeekEndDateLessThanEqualOrderByWeekStartDateAsc(
            Long userId,
            WeeklyReportStatus status,
            LocalDate snapshotDate
    );

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WeeklyReport w SET w.status = :status WHERE w.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") WeeklyReportStatus status
    );

    /**
     * 특정 월(monthStart ~ monthEnd)과 겹치는 주간 리포트 조회
     * 월간 리포트 생성을 위해 사용
     */
    @Query("""
        select wr
        from WeeklyReport wr
        where wr.user.id = :userId
          and wr.weekStartDate <= :monthEnd
          and wr.weekEndDate >= :monthStart
          and wr.status = :status
        order by wr.weekStartDate asc
    """)
    List<WeeklyReport> findMonthlyOverlappedWeeklyReports(
            @Param("userId") Long userId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("status") WeeklyReportStatus status
    );
}