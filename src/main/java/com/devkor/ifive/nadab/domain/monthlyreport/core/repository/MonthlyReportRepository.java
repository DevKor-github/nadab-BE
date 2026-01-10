package com.devkor.ifive.nadab.domain.monthlyreport.core.repository;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    Optional<MonthlyReport> findByUserIdAndMonthStartDate(Long userId, LocalDate monthStartDate);

    /**
     * PENDING -> COMPLETED 확정
     * - 분석 결과(discovered/good/improve) 저장
     * - analyzedAt 기록
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE MonthlyReport mr
       SET mr.status = :status,
           mr.discovered = :discovered,
           mr.good = :good,
           mr.improve = :improve,
           mr.analyzedAt = CURRENT_TIMESTAMP
     WHERE mr.id = :reportId
""")
    int markCompleted(
            @Param("reportId") Long reportId,
            @Param("status") MonthlyReportStatus status,
            @Param("discovered") String discovered,
            @Param("good") String good,
            @Param("improve") String improve
    );

    /**
     * PENDING -> FAILED 확정
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE MonthlyReport mr
       SET mr.status = :status,
           mr.analyzedAt = CURRENT_TIMESTAMP
     WHERE mr.id = :reportId
""")
    int markFailed(
            @Param("reportId") Long reportId,
            @Param("status") MonthlyReportStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MonthlyReport m SET m.status = :status WHERE m.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") MonthlyReportStatus status
    );
}
