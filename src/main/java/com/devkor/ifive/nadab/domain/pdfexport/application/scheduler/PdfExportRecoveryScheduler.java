package com.devkor.ifive.nadab.domain.pdfexport.application.scheduler;

import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.application.event.PdfExportFailedEvent;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 진행 중인 채로 멈춘 PDF 작업을 회수한다. 렌더를 다시 돌리지 않고 실패 확정 + 환불만 한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PdfExportRecoveryScheduler {

    /** 이 시간을 넘겨 진행 중이면 멈춘 것으로 본다. 기준 시각이 큐 진입 시점이라 대기 시간까지 덮어야 함 */
    private static final Duration STUCK_TIMEOUT = Duration.ofMinutes(60);

    /** 한 번에 처리할 최대 건수 */
    private static final int BATCH_SIZE = 100;

    private final PdfExportJobRepository pdfExportJobRepository;
    private final PdfExportTxService txService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void recoverStuckJobs() {
        try {
            OffsetDateTime threshold = OffsetDateTime.now().minus(STUCK_TIMEOUT);
            List<PdfExportJob> stuck = pdfExportJobRepository.findStuckInProgress(threshold, BATCH_SIZE);
            if (stuck.isEmpty()) {
                return;
            }

            List<Long> recovered = new ArrayList<>();
            List<Long> failed = new ArrayList<>();
            for (PdfExportJob job : stuck) {
                if (refundQuietly(job, failed)) {
                    recovered.add(job.getId());
                }
            }
            log.warn("[PDF_EXPORT][RECOVERY] 멈춘 작업 회수: 대상={}건, 환불성공={}, 환불실패={}",
                    stuck.size(), recovered, failed);

        } catch (Exception e) {
            // 스케줄러 스레드로 예외가 새면 다음 주기가 안 돌 수 있다.
            log.error("[PDF_EXPORT][RECOVERY] 스윕 실패", e);
        }
    }

    /**
     * 한 건 환불. 실패해도 삼키고 다음 건으로 넘어가며, 삼킨 건은 IN_PROGRESS로 남아 다음 주기에 다시 대상이 된다.
     * 배치 전체가 같은 이유로 실패하는 일이 5분마다 반복될 수 있어 스택은 주기마다 첫 건에만 남긴다(나머지 jobId는 요약 줄에).
     */
    private boolean refundQuietly(PdfExportJob job, List<Long> failed) {
        Long jobId = job.getId();
        try {
            Long userId = job.getUser().getId();   // LAZY 프록시 id 접근 — 쿼리 없음
            boolean refunded = txService.failAndRefund(userId, jobId, job.getCrystalLogId(),
                    ErrorCode.PDF_EXPORT_GENERATION_TIMEOUT.name());
            if (refunded) {
                // 이 경로는 정의상 사용자가 생성 화면을 떠난 뒤다(최소 60분 경과) — 알림이 유일한 통보 수단이다.
                notifyFailed(jobId, userId);
            }
            // false 면 그 사이 렌더 리스너가 먼저 정리한 것이라 우리가 회수한 건이 아니다.
            return refunded;
        } catch (Exception e) {
            if (failed.isEmpty()) {
                log.error("[PDF_EXPORT][RECOVERY] 개별 환불 실패: jobId={}", jobId, e);
            }
            failed.add(jobId);
            return false;
        }
    }

    /**
     * 실패 알림 발행. 환불은 이미 커밋된 뒤다.
     * 예외가 새면 위 catch 가 환불 성공을 실패로 집계하므로 여기서 삼킨다.
     */
    private void notifyFailed(Long jobId, Long userId) {
        try {
            eventPublisher.publishEvent(new PdfExportFailedEvent(jobId, userId));
        } catch (Exception e) {
            log.warn("[PDF_EXPORT][RECOVERY] 알림 발행 실패: jobId={} — 환불은 정상 처리됐다", jobId, e);
        }
    }
}