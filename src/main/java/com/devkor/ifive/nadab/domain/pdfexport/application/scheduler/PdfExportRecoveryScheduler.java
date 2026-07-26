package com.devkor.ifive.nadab.domain.pdfexport.application.scheduler;

import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
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

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void recoverStuckJobs() {
        try {
            OffsetDateTime threshold = OffsetDateTime.now().minus(STUCK_TIMEOUT);
            List<PdfExportJob> stuck = pdfExportJobRepository.findStuckInProgress(threshold, BATCH_SIZE);
            if (stuck.isEmpty()) {
                return;
            }

            int recovered = 0;
            for (PdfExportJob job : stuck) {
                if (refundQuietly(job)) {
                    recovered++;
                }
            }
            log.warn("[PDF_EXPORT][RECOVERY] 멈춘 작업 환불: 대상={}, 환불={}, 임계={}분",
                    stuck.size(), recovered, STUCK_TIMEOUT.toMinutes());

        } catch (Exception e) {
            // 스케줄러 스레드로 예외가 새면 다음 주기가 안 돌 수 있다.
            log.error("[PDF_EXPORT][RECOVERY] 스윕 실패", e);
        }
    }

    /**
     * 한 건 환불. 실패해도 삼키고 다음 건으로 넘어간다.
     * 삼킨 건은 상태가 IN_PROGRESS로 남아 다음 주기에 다시 대상이 된다.
     */
    private boolean refundQuietly(PdfExportJob job) {
        Long jobId = job.getId();
        try {
            Long userId = job.getUser().getId();   // LAZY 프록시 id 접근 — 쿼리 없음
            txService.failAndRefund(userId, jobId, job.getCrystalLogId(),
                    ErrorCode.PDF_EXPORT_GENERATION_TIMEOUT.name());
            return true;
        } catch (Exception e) {
            log.error("[PDF_EXPORT][RECOVERY] 개별 환불 실패: jobId={}", jobId, e);
            return false;
        }
    }
}