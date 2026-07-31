package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportRequestedEventDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;

/**
 * PDF 내보내기 과금/상태 전이 트랜잭션
 * - reserveAndPublish: job(PENDING) 생성 → 원자적 선차감(tryConsume) → CrystalLog(PENDING)
 *   → job IN_PROGRESS + crystalLogId 기록 → (AFTER_COMMIT 보장 위해) 트랜잭션 안에서 이벤트 발행
 * - confirm: job COMPLETED + CrystalLog CONFIRMED
 * - failAndRefund: job FAILED + 환불(CrystalLog.delta) + CrystalLog REFUNDED
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PdfExportTxService {

    private final PdfExportJobRepository pdfExportJobRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;
    private final PdfExportStorage pdfExportStorage;

    private final ApplicationEventPublisher eventPublisher;

    private static final String REF_TYPE = "PDF_EXPORT_JOB";

    public PdfExportReserveResultDto reserveAndPublish(User user, PdfExportType type,
                                                       LocalDate startDate, LocalDate endDate) {
        long cost = type.getCrystalCost();

        // job(PENDING) 생성 (id 확보). 열거불가 키를 이 시점에 각인(업로드는 리스너가 이 키로 수행).
        String resultKey = pdfExportStorage.newResultKey(user.getId());
        PdfExportJob job = pdfExportJobRepository.save(
                PdfExportJob.createPending(user, type, startDate, endDate, resultKey));
        Long jobId = job.getId();

        // 원자적 선차감
        int updated = userWalletRepository.tryConsume(user.getId(), cost);
        if (updated == 0) {
            throw new NotEnoughCrystalException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
        }

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();

        // 차감 로그(PENDING)
        CrystalLog crystalLog = crystalLogRepository.save(
                CrystalLog.createPending(user, -cost, balanceAfter, CrystalLogReason.PDF_EXPORT_GENERATE, REF_TYPE, jobId)
        );

        // job을 IN_PROGRESS로 바꾸고 차감 로그 id를 기록한다.
        // 방금 같은 트랜잭션에서 만든 job이라 정상이면 반드시 1건이 바뀐다.
        // 0이면 있을 수 없는 상황이므로, 예외를 던져 트랜잭션 전체를 롤백한다(차감·job 생성 되돌림 → 돈만 빠지는 일 방지).
        if (pdfExportJobRepository.startProcessing(jobId, crystalLog.getId()) == 0) {
            throw new IllegalStateException("PDF 작업을 진행 중 상태로 바꾸지 못했습니다. jobId=" + jobId);
        }

        // 트랜잭션 안에서 publish (AFTER_COMMIT 트리거 보장)
        eventPublisher.publishEvent(new PdfExportRequestedEventDto(jobId, user.getId(), crystalLog.getId()));

        return new PdfExportReserveResultDto(jobId, balanceAfter);
    }

    public void confirm(Long jobId, Long logId) {
        // 진행 중일 때만 성공(1). 0이면 그 사이 다른 곳(복구 스윕)에서 이미 실패·환불된 job이라 확정하지 않는다.
        if (pdfExportJobRepository.markCompleted(jobId) == 0) {
            log.warn("[PDF_EXPORT][CONFIRM_SKIPPED] jobId={} — 이미 실패·환불 처리된 작업이라 확정하지 않는다", jobId);
            return;
        }
        crystalLogRepository.markConfirmed(logId);
        dedupPreviousCompleted(jobId);
    }

    /**
     * dedup: 동일 (user·type·기간)의 이전 COMPLETED를 제거해 아카이브에 최근 것만 남긴다.
     * row는 이 트랜잭션에서 삭제하고, S3 객체는 커밋 이후 best-effort로 지운다(느린 외부 I/O를 완료 Tx 밖으로).
     * crystal_logs는 job과 독립 보존(감사)이라 row 삭제는 무해.
     */
    private void dedupPreviousCompleted(Long jobId) {
        // markCompleted가 영속성 컨텍스트를 비워(clearAutomatically) 방금 COMPLETED된 job을 다시 읽는다.
        PdfExportJob job = pdfExportJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PDF_EXPORT_JOB_NOT_FOUND));
        Long userId = job.getUser().getId(); // LAZY 프록시 id 접근 — 쿼리 없음

        List<String> staleKeys = pdfExportJobRepository.findStaleCompletedResultKeys(
                userId, job.getType(), job.getStartDate(), job.getEndDate(), jobId);
        if (staleKeys.isEmpty()) {
            return;
        }

        pdfExportJobRepository.deleteStaleCompleted(
                userId, job.getType(), job.getStartDate(), job.getEndDate(), jobId);
        registerAfterCommitS3Delete(staleKeys);
    }

    private void registerAfterCommitS3Delete(List<String> keys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keys.forEach(pdfExportStorage::delete);
            }
        });
    }

    /**
     * 실패 확정 + 환불. 반환값은 이 호출이 실제로 환불했는지다.
     * 호출자(렌더 리스너·복구 스케줄러)는 이 값이 true 일 때만 실패 알림을 발행한다.
     * false 는 CAS 경합에서 져 아무것도 안 한 경우로, 그때 알리면 이미 완료된 작업에 "생성에 실패했어요" 푸시가 나간다.
     */
    public boolean failAndRefund(Long userId, Long jobId, Long logId, String errorCode) {
        // 진행 중일 때만 성공(1)한 호출만 환불을 책임진다. 0이면 이미 완료·환불된 job → 공짜 PDF·이중 환불 방지.
        if (pdfExportJobRepository.markFailed(jobId, errorCode) == 0) {
            log.warn("[PDF_EXPORT][REFUND_SKIPPED] jobId={}, errorCode={} — 이미 완료·환불된 작업이라 환불하지 않는다",
                    jobId, errorCode);
            return false;
        }

        // 실제 차감된 값(CrystalLog.delta, 음수)을 그대로 되돌린다 — 로그에서 읽어 가격이 바뀌어도 차감액=환불액 보장.
        CrystalLog crystalLog = crystalLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CRYSTAL_LOG_NOT_FOUND));
        long refundAmount = -crystalLog.getDelta();

        int updated = userWalletRepository.refund(userId, refundAmount);
        if (updated == 0) {
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        crystalLogRepository.markRefunded(logId);
        log.warn("[PDF_EXPORT][REFUNDED] jobId={}, userId={}, refund={}, errorCode={}",
                jobId, userId, refundAmount, errorCode);
        return true;
    }
}