package com.devkor.ifive.nadab.domain.notification.application.scheduler;

import com.devkor.ifive.nadab.domain.notification.application.helper.NotificationTransactionHelper;
import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 알림 복구 스케줄러
 * - SENDING 상태에서 멈춘 알림 복구
 * - 5분 이상 SENDING 상태면 타임아웃으로 간주하고 재시도
 * - 1분마다 실행
 * - Atomic Operation으로 동시성 제어 (Lost Update 방지)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRecoveryScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationTransactionHelper transactionHelper;

    private static final int TIMEOUT_MINUTES = 5;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 1;  // DEAD_LETTER 비율 모니터링 후 필요시 증가

    /**
     * SENDING 상태 타임아웃 복구
     * - 1분마다 실행
     * - 5분 이상 SENDING 상태면 재시도
     */
    @Scheduled(fixedDelay = 60000)
    public void recoverStuckNotifications() {
        try {
            OffsetDateTime timeoutThreshold = OffsetDateTime.now().minusMinutes(TIMEOUT_MINUTES);

            List<Notification> stuckList = notificationRepository
                .findStuckNotificationsForRecovery(
                    NotificationStatus.SENDING,
                    timeoutThreshold,
                    BATCH_SIZE
                );

            if (stuckList.isEmpty()) {
                return;
            }

            log.debug("Recovering {} stuck SENDING notifications (timeout: {} minutes)",
                stuckList.size(), TIMEOUT_MINUTES);

            int recoveredToSent = 0;
            int recoveredToPending = 0;
            int recoveredToDeadLetter = 0;
            int alreadyProcessed = 0;

            for (Notification notification : stuckList) {
                RecoveryResult result = recoverNotification(notification);
                switch (result) {
                    case SENT -> recoveredToSent++;
                    case PENDING -> recoveredToPending++;
                    case DEAD_LETTER -> recoveredToDeadLetter++;
                    case ALREADY_PROCESSED -> alreadyProcessed++;
                }
            }

            log.debug("Recovery completed: SENT={}, PENDING={}, DEAD_LETTER={}, alreadyProcessed={}",
                recoveredToSent, recoveredToPending, recoveredToDeadLetter, alreadyProcessed);

        } catch (Exception e) {
            log.error("Failed to recover stuck SENDING notifications", e);
        }
    }

    /**
     * 개별 알림 복구
     * - fcmSent 체크로 중복 발송 방지
     * - fcmSent = true: FCM 이미 발송됨, 상태만 SENT로
     * - fcmSent = false: FCM 발송 안 됨, 재시도
     * - TransactionHelper로 트랜잭션 분리
     */
    private RecoveryResult recoverNotification(Notification notification) {
        Long notificationId = notification.getId();

        // 1. fcmSent=true → SENT 시도
        boolean markedAsSent = transactionHelper.markAsSentIfFcmSent(notificationId);
        if (markedAsSent) {
            return RecoveryResult.SENT;
        }

        // 2. fcmSent=false → PENDING 복구 시도
        boolean recovered = transactionHelper.recoverStuckNotification(notificationId, MAX_RETRY_COUNT);

        if (recovered) {
            // 성공: retry_count 증가 - DB에서 최신 값 조회해서 판단
            Notification fresh = notificationRepository.findById(notificationId).orElse(null);

            if (fresh != null && fresh.getRetryCount() >= MAX_RETRY_COUNT) {
                log.warn("⚠️ Stuck notification moved to DEAD_LETTER: id={}, retryCount={}",
                    notificationId, fresh.getRetryCount());
                return RecoveryResult.DEAD_LETTER;
            } else if (fresh != null) {
                log.info("🔄 Stuck notification reset to PENDING: id={}, retryCount={}",
                    notificationId, fresh.getRetryCount());
                return RecoveryResult.PENDING;
            }
        }

        // 둘 다 실패 = 다른 스레드가 처리함
        log.debug("Notification already processed by another thread: id={}", notificationId);
        return RecoveryResult.ALREADY_PROCESSED;
    }

    /**
     * 복구 결과 Enum
     */
    protected enum RecoveryResult {
        SENT,               // FCM 발송 완료 → SENT
        PENDING,            // FCM 발송 안 됨 → PENDING (재시도)
        DEAD_LETTER,        // 최대 재시도 횟수 초과 → DEAD_LETTER
        ALREADY_PROCESSED   // 이미 다른 스레드가 처리함
    }
}