package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @Modifying
    @Query("""
        UPDATE DailyReport r
        SET r.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED,
            r.content = :content
        WHERE r.id = :reportId
    """)
    int markCompleted(@Param("reportId") Long reportId, @Param("content") String content);

    @Modifying
    @Query("""
        UPDATE DailyReport r
        SET r.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.FAILED
        WHERE r.id = :reportId
    """)
    int markFailed(@Param("reportId") Long reportId);
}
