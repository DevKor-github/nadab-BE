package com.devkor.ifive.nadab.domain.moderation.core.repository;

import com.devkor.ifive.nadab.domain.moderation.core.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    boolean existsByReporterIdAndDailyReportId(Long reporterId, Long dailyReportId);

    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

    /**
     * reportedUser를 신고한 전체 누적 건수
     */
    @Query("""
        SELECT COUNT(cr.id)
        FROM ContentReport cr
        WHERE cr.reportedUser.id = :reportedUserId
        """)
    long countAllReports(@Param("reportedUserId") Long reportedUserId);

    /**
     * reportedUser를 신고한 전체 누적 distinct 신고자 수
     */
    @Query("""
        SELECT COUNT(DISTINCT cr.reporter.id)
        FROM ContentReport cr
        WHERE cr.reportedUser.id = :reportedUserId
        """)
    long countAllDistinctReporters(@Param("reportedUserId") Long reportedUserId);

    /**
     * reportedUser를 신고한 건수 (since 이후 누적)
     */
    @Query("""
        SELECT COUNT(cr.id)
        FROM ContentReport cr
        WHERE cr.reportedUser.id = :reportedUserId
          AND cr.createdAt > :since
        """)
    long countReportsSince(@Param("reportedUserId") Long reportedUserId,
                           @Param("since") OffsetDateTime since);

    /**
     * reportedUser를 신고한 distinct 신고자 수 (since 이후 누적)
     */
    @Query("""
        SELECT COUNT(DISTINCT cr.reporter.id)
        FROM ContentReport cr
        WHERE cr.reportedUser.id = :reportedUserId
          AND cr.createdAt > :since
        """)
    long countDistinctReportersSince(@Param("reportedUserId") Long reportedUserId,
                                     @Param("since") OffsetDateTime since);

    /**
     * 내가 신고한 DailyReport ID 목록 조회
     */
    @Query("""
        SELECT cr.dailyReport.id
        FROM ContentReport cr
        WHERE cr.reporter.id = :reporterId
          AND cr.dailyReport.id IN :dailyReportIds
        """)
    List<Long> findReportedDailyReportIdsByReporter(
        @Param("reporterId") Long reporterId,
        @Param("dailyReportIds") List<Long> dailyReportIds
    );
}