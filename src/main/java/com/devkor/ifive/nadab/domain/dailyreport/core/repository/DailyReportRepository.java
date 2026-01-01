package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE DailyReport r
        SET r.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED,
            r.content = :content,
            r.emotion.id = :emotionId,
            r.analyzedAt = CURRENT_TIMESTAMP
        WHERE r.id = :reportId
    """)
    int markCompleted(@Param("reportId") Long reportId, @Param("content") String content, @Param("emotionId") Long emotionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE DailyReport r
        SET r.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.FAILED
        WHERE r.id = :reportId
    """)
    int markFailed(@Param("reportId") Long reportId);

    Optional<DailyReport> findByAnswerEntryAndCreatedAtBetween(AnswerEntry answerEntry, OffsetDateTime start, OffsetDateTime end);
}
