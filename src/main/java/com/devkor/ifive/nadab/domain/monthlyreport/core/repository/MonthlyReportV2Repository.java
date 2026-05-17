package com.devkor.ifive.nadab.domain.monthlyreport.core.repository;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MonthlyReportV2Repository extends JpaRepository<MonthlyReportV2, Long> {

    boolean existsByUserIdAndStatus(Long userId, MonthlyReportStatus status);

    Optional<MonthlyReportV2> findByUserIdAndMonthStartDate(Long userId, LocalDate monthStartDate);

    Optional<MonthlyReportV2> findByUserIdAndMonthStartDateAndStatus(
            Long userId,
            LocalDate monthStartDate,
            MonthlyReportStatus status
    );

    List<MonthlyReportV2> findAllByUserIdAndStatus(Long userId, MonthlyReportStatus status);

    /**
     * PENDING -> FAILED 확정
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE MonthlyReportV2 mr
       SET mr.status = :status,
           mr.analyzedAt = CURRENT_TIMESTAMP
     WHERE mr.id = :reportId
""")
    int markFailed(
            @Param("reportId") Long reportId,
            @Param("status") MonthlyReportStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MonthlyReportV2 m SET m.status = :status WHERE m.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") MonthlyReportStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE monthly_reports_v2
       SET content = CAST(:contentJson AS jsonb),
           emotion_summary_content = CAST(:emotionSummaryContentJson AS jsonb),
           emotion_stats = CAST(:emotionStatsJson AS jsonb),
           interest_stats = CAST(:interestStatsJson AS jsonb),
           summary = :summary,
           comment_summary = :commentSummary,
           dominant_keyword = :dominantKeyword,
           status = :status,
           analyzed_at = CURRENT_TIMESTAMP
     WHERE id = :reportId
""", nativeQuery = true)
    int updateContent(
            @Param("reportId") Long reportId,
            @Param("contentJson") String contentJson,
            @Param("emotionSummaryContentJson") String emotionSummaryContentJson,
            @Param("summary") String summary,
            @Param("commentSummary") String commentSummary,
            @Param("dominantKeyword") String dominantKeyword,
            @Param("emotionStatsJson") String emotionStatsJson,
            @Param("interestStatsJson") String interestStatsJson,
            @Param("status") String status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE MonthlyReportV2 mr
       SET mr.imageStatus = :imageStatus
     WHERE mr.id = :reportId
""")
    int updateImageStatus(
            @Param("reportId") Long reportId,
            @Param("imageStatus") MonthlyReportImageStatus imageStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE MonthlyReportV2 mr
       SET mr.imageKey = :imageKey,
           mr.imageStatus = :imageStatus,
           mr.status = :status
     WHERE mr.id = :reportId
""")
    int completeWithImage(
            @Param("reportId") Long reportId,
            @Param("imageKey") String imageKey,
            @Param("imageStatus") MonthlyReportImageStatus imageStatus,
            @Param("status") MonthlyReportStatus status
    );
}
