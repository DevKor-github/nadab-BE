package com.devkor.ifive.nadab.domain.moderation.core.repository;

import com.devkor.ifive.nadab.domain.moderation.core.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    /**
     * 중복 신고 체크
     */
    boolean existsByReporterIdAndDailyReportId(Long reporterId, Long dailyReportId);

    /**
     * 특정 유저에 대한 신고 건수 조회
     */
    long countByReportedUserId(Long reportedUserId);

    /**
     * 특정 유저를 신고한 사람 수 (중복 제거)
     */
    @Query("""
        SELECT COUNT(DISTINCT cr.reporter.id)
        FROM ContentReport cr
        WHERE cr.reportedUser.id = :reportedUserId
        """)
    long countDistinctReportersByReportedUserId(@Param("reportedUserId") Long reportedUserId);

    /**
     * 공유 활동 중지 대상 유저 ID 조회 (신고 10건 이상 && 신고자 2명 이상)
     */
    @Query("""
        SELECT cr.reportedUser.id
        FROM ContentReport cr
        WHERE cr.reportedUser.id IN :userIds
        GROUP BY cr.reportedUser.id
        HAVING COUNT(cr.id) >= 10
           AND COUNT(DISTINCT cr.reporter.id) >= 2
        """)
    List<Long> findSharingSuspendedUserIds(@Param("userIds") List<Long> userIds);

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