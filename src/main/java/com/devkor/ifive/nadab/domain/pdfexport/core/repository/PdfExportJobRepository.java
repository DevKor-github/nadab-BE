package com.devkor.ifive.nadab.domain.pdfexport.core.repository;

import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PdfExportJobRepository extends JpaRepository<PdfExportJob, Long> {

    /**
     * 아카이브(이력) 목록: 주어진 상태의 작업을 최신 생성순으로 반환. user는 접근하지 않아 LAZY 초기화 없음.
     */
    @Query("""
        SELECT j FROM PdfExportJob j
         WHERE j.user.id = :userId
           AND j.status IN :statuses
         ORDER BY j.createdAt DESC
    """)
    List<PdfExportJob> findArchive(
            @Param("userId") Long userId,
            @Param("statuses") Collection<PdfExportStatus> statuses
    );

    /**
     * 유저가 지금 진행 중인(PENDING/IN_PROGRESS) 작업. 유니크 인덱스가 유저당 1개로 보장해 단건이다.
     * 같은 조건 재요청이면 이 작업을 재과금 없이 다시 쓰고, 다른 조건이면 동시 1개 제한으로 거부한다.
     * statuses엔 PENDING/IN_PROGRESS를 넘긴다.
     */
    @Query("""
        SELECT j FROM PdfExportJob j
         WHERE j.user.id = :userId
           AND j.status IN :statuses
    """)
    Optional<PdfExportJob> findActiveJob(
            @Param("userId") Long userId,
            @Param("statuses") Collection<PdfExportStatus> statuses
    );

    /**
     * 작업을 '진행 중(IN_PROGRESS)'으로 바꾸고 차감 로그 id를 연결한다. (크리스탈 차감 직후 호출)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PdfExportJob j
           SET j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.IN_PROGRESS,
               j.crystalLogId = :crystalLogId,
               j.updatedAt = CURRENT_TIMESTAMP
         WHERE j.id = :id
    """)
    int startProcessing(@Param("id") Long id, @Param("crystalLogId") Long crystalLogId);

    /**
     * 작업을 '완료(COMPLETED)'로 바꾸고 완료 시각을 각인한다. 결과 키는 생성 시점에 이미 각인돼 있다.
     * 진행 중일 때만 바뀌며, 실제로 바꾼 행 수를 반환한다(0이면 이미 다른 곳에서 처리됨 — 실패/환불 등).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PdfExportJob j
           SET j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.COMPLETED,
               j.completedAt = CURRENT_TIMESTAMP,
               j.updatedAt = CURRENT_TIMESTAMP
         WHERE j.id = :id
           AND j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.IN_PROGRESS
    """)
    int markCompleted(@Param("id") Long id);

    /**
     * 작업을 '실패(FAILED)'로 바꾸고 실패 코드를 기록한다.
     * 진행 중일 때만 바뀌며, 실제로 바꾼 행 수를 반환한다(0이면 이미 완료 등으로 처리됨).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PdfExportJob j
           SET j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.FAILED,
               j.errorCode = :errorCode,
               j.updatedAt = CURRENT_TIMESTAMP
         WHERE j.id = :id
           AND j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.IN_PROGRESS
    """)
    int markFailed(@Param("id") Long id, @Param("errorCode") String errorCode);

    /**
     * dedup: 방금 완료된 job(keepId)과 동일 (user·type·기간)의 이전 COMPLETED 결과 키들. S3 삭제 대상.
     * 동시 완료는 부분 유니크 인덱스(PENDING/IN_PROGRESS)가 직렬화하므로 경합 없이 '이전' 것만 잡힌다.
     */
    @Query("""
        SELECT j.resultKey FROM PdfExportJob j
         WHERE j.user.id = :userId
           AND j.type = :type
           AND j.startDate = :startDate
           AND j.endDate = :endDate
           AND j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.COMPLETED
           AND j.id <> :keepId
    """)
    List<String> findStaleCompletedResultKeys(
            @Param("userId") Long userId,
            @Param("type") PdfExportType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keepId") Long keepId
    );

    /** dedup: 위 findStaleCompletedResultKeys와 동일 조건의 이전 COMPLETED row 삭제(최근 것만 아카이브에 남김). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM PdfExportJob j
         WHERE j.user.id = :userId
           AND j.type = :type
           AND j.startDate = :startDate
           AND j.endDate = :endDate
           AND j.status = com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus.COMPLETED
           AND j.id <> :keepId
    """)
    int deleteStaleCompleted(
            @Param("userId") Long userId,
            @Param("type") PdfExportType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keepId") Long keepId
    );
}