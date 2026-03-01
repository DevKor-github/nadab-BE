package com.devkor.ifive.nadab.domain.notification.application.helper;

import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 트랜잭션 헬퍼
 * - FCM 발송을 트랜잭션 밖으로 빼기 위한 별도 트랜잭션 처리
 * - DB 커넥션을 빠르게 반환
 * - 낙관적 fcmSent 설정으로 중복 발송 완전 방지
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationTransactionHelper {

    private final NotificationRepository notificationRepository;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * EventListener: PENDING → SENDING & fcmSent = true (낙관적 설정, Atomic Operation)
     * - FCM 발송 전에 미리 fcmSent = true로 설정
     * - FCM 발송 실패 시 updateFinalStatus()에서 false로 복구
     * - WHERE 조건으로 PENDING 상태만 변경 (중복 처리 방지)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsSendingWithFcmFlag(Long notificationId) {
        // PENDING → SENDING & fcmSent=true (WHERE status=PENDING)
        int updated = notificationRepository.markAsSendingWithFcmFlag(notificationId);

        if (updated == 0) {
            // PENDING이 아님 (이미 처리 중이거나 완료)
            log.warn("Notification is not PENDING, skip marking as SENDING: id={}", notificationId);
            throw new IllegalStateException("Notification is not in PENDING status: " + notificationId);
        }

        log.debug("Notification marked as SENDING with FCM flag: id={}", notificationId);
    }

    /**
     * RetryScheduler: FAILED → SENDING & fcmSent = true & retry_count++ (낙관적 설정, Atomic Operation)
     * - FCM 발송 전에 미리 fcmSent = true로 설정
     * - retry_count 증가 (재시도 시작 시점에 카운트)
     * - FCM 발송 실패 시 updateFinalStatus()에서 fcmSent를 false로 복구 (retry_count는 유지)
     * - WHERE 조건으로 FAILED 상태만 변경 (중복 처리 방지)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedAsSendingWithFcmFlag(Long notificationId) {
        // FAILED → SENDING & fcmSent=true & retry_count++ (WHERE status=FAILED)
        int updated = notificationRepository.markFailedAsSendingWithFcmFlag(notificationId);

        if (updated == 0) {
            // FAILED가 아님 (이미 처리 중이거나 완료)
            log.warn("Notification is not FAILED, skip marking as SENDING: id={}", notificationId);
            throw new IllegalStateException("Notification is not in FAILED status: " + notificationId);
        }

        log.debug("FAILED notification marked as SENDING with FCM flag and retry_count incremented: id={}", notificationId);
    }

    /**
     * T3: FCM 발송 결과에 따른 최종 상태 업데이트
     * - 성공: SENDING → SENT (fcmSent=true 유지, retry_count 유지)
     * - 실패: SENDING → FAILED & fcmSent=false (retry_count는 유지 - 이미 T1에서 증가됨)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateFinalStatus(Long notificationId, boolean fcmSuccess) {
        if (fcmSuccess) {
            // FCM 발송 성공 → SENT
            int updated = notificationRepository.updateStatusConditionally(
                notificationId,
                NotificationStatus.SENDING,
                NotificationStatus.SENT
            );

            if (updated == 1) {
                log.info("✅ Notification sent successfully: id={}", notificationId);
                return true;
            } else {
                log.warn("Status already changed during send: id={}, will be recovered by RecoveryScheduler",
                    notificationId);
                return false;
            }
        } else {
            // FCM 발송 실패 → FAILED & fcmSent=false (retry_count 유지)
            int updated = notificationRepository.markAsFailedAndResetFcm(notificationId);

            if (updated == 1) {
                log.warn("⚠️ Notification marked as FAILED: id={}", notificationId);
                return true;
            } else {
                log.error("Failed to mark as FAILED: id={}", notificationId);
                return false;
            }
        }
    }

    /**
     * 예외 발생 시 FAILED 상태로 복구 (T3 대체 경로)
     * - T2 이후 예외 발생 시 호출 (FCM 발송 실패, T3 실패 등)
     * - SENDING → FAILED & fcmSent = false (재시도 가능하도록 복구)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(Long notificationId) {
        try {
            int updated = notificationRepository.markAsFailedAndResetFcm(notificationId);
            if (updated == 1) {
                log.warn("Notification marked as FAILED after exception: id={}", notificationId);
            } else {
                log.error("Failed to mark notification as FAILED: id={}", notificationId);
            }
        } catch (Exception e) {
            log.error("Exception while marking notification as FAILED: id={}, error={}",
                notificationId, e.getMessage(), e);
        }
    }

    /**
     * RetryScheduler: retry_count >= MAX → DEAD_LETTER
     * - 최대 재시도 횟수 초과 시 재시도 포기
     * - FAILED → DEAD_LETTER (일괄 처리)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int moveFailedToDeadLetter() {
        int count = notificationRepository.moveFailedToDeadLetter(MAX_RETRY_COUNT);
        if (count > 0) {
            log.warn("Moved {} FAILED notifications to DEAD_LETTER (retry_count >= {})",
                count, MAX_RETRY_COUNT);
        }
        return count;
    }

    /**
     * RecoveryScheduler: SENDING → SENT (fcmSent=true인 경우)
     * - FCM 발송은 완료되었지만 상태 업데이트가 실패한 경우
     * - WHERE fcmSent=true 조건으로 안전하게 SENT로 변경
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markAsSentIfFcmSent(Long notificationId) {
        int updated = notificationRepository.markAsSentIfFcmSent(notificationId);

        if (updated == 1) {
            log.info("✅ FCM already sent, marked as SENT: id={}", notificationId);
            return true;
        }

        return false;
    }

    /**
     * RecoveryScheduler: SENDING → PENDING/DEAD_LETTER (fcmSent=false인 경우)
     * - FCM 발송이 안 되었거나 실패한 경우 재시도
     * - retry_count 증가
     * - MAX 초과 시 DEAD_LETTER로 이동
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recoverStuckNotification(Long notificationId, int maxRetryCount) {
        int updated = notificationRepository.recoverStuckNotification(notificationId, maxRetryCount);

        if (updated == 1) {
            return true;
        }

        return false;
    }
}