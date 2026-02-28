package com.devkor.ifive.nadab.domain.notification.application.scheduler;

import com.devkor.ifive.nadab.domain.notification.application.helper.NotificationTransactionHelper;
import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import com.devkor.ifive.nadab.domain.notification.infra.FcmNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 알림 재시도 스케줄러
 * - EventListener 실패 시 PENDING 알림 재시도
 * - FAILED 알림 재시도 (Exponential Backoff)
 * - 10초마다 실행
 * - 조건부 UPDATE로 중복 방지
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final FcmNotificationSender fcmNotificationSender;
    private final NotificationTransactionHelper transactionHelper;

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * Exponential Backoff 재시도 전략
     * - retry_count별 대기 시간:
     *   0회: 10초
     *   1회: 20초
     *   2회: 40초 (최대 3회 재시도)
     */
    private static final int[] RETRY_DELAYS_SECONDS = {10, 20, 40};

    /**
     * 알림 재시도
     * - 10초마다 실행
     * - PENDING 재시도 (EventListener Fallback)
     * - FAILED 재시도 (Exponential Backoff)
     */
    @Scheduled(fixedDelay = 10000)
    public void retryNotifications() {
        try {
            // 1. PENDING 알림 재시도 (EventListener Fallback)
            retryPendingNotifications();

            // 2. FAILED 알림 재시도 (Exponential Backoff)
            retryFailedNotifications();

        } catch (Exception e) {
            log.error("Failed to retry notifications", e);
        }
    }

    /**
     * PENDING 알림 재시도 (EventListener Fallback)
     * - EventListener 실패 시 PENDING 상태로 남은 알림 처리
     * - 100개씩 배치 처리
     */
    private void retryPendingNotifications() {
        List<Notification> pendingList = notificationRepository
            .findByStatusOrderByIdAsc(NotificationStatus.PENDING, BATCH_SIZE);

        if (pendingList.isEmpty()) {
            return;
        }

        log.info("Retrying {} PENDING notifications (EventListener Fallback)", pendingList.size());

        // 배치 처리
        processBatch(pendingList, NotificationStatus.PENDING);
    }

    /**
     * FAILED 알림 재시도 (Exponential Backoff)
     * - retry_count별로 다른 대기 시간 적용
     * - FAILED → 바로 재발송 (PENDING 거치지 않음)
     * - retry_count >= MAX_RETRY_COUNT → DEAD_LETTER
     */
    private void retryFailedNotifications() {
        OffsetDateTime now = OffsetDateTime.now();
        int totalRetried = 0;

        // 1. retry_count별로 재시도 (Exponential Backoff: 0, 1, 2)
        for (int retryCount = 0; retryCount < MAX_RETRY_COUNT; retryCount++) {
            int retried = retryFailedByRetryCount(retryCount, now);
            totalRetried += retried;
        }

        // 2. retry_count >= MAX_RETRY_COUNT인 FAILED → DEAD_LETTER (별도 트랜잭션)
        transactionHelper.moveFailedToDeadLetter();

        if (totalRetried > 0) {
            log.info("Retried {} FAILED notifications with Exponential Backoff", totalRetried);
        }
    }

    /**
     * retry_count별 FAILED 알림 재시도
     */
    protected int retryFailedByRetryCount(int retryCount, OffsetDateTime now) {
        // Exponential Backoff 대기 시간 계산
        int delaySeconds = RETRY_DELAYS_SECONDS[Math.min(retryCount, RETRY_DELAYS_SECONDS.length - 1)];
        OffsetDateTime threshold = now.minusSeconds(delaySeconds);

        // updated_at < threshold인 FAILED 알림 조회
        List<Notification> failedList = notificationRepository
            .findByStatusAndRetryCountAndUpdatedAtBefore(
                NotificationStatus.FAILED,
                retryCount,
                threshold,
                BATCH_SIZE
            );

        if (failedList.isEmpty()) {
            return 0;
        }

        log.info("Retrying {} FAILED notifications: retryCount={}, delay={}s",
            failedList.size(), retryCount, delaySeconds);

        // FAILED → 바로 재발송
        processBatch(failedList, NotificationStatus.FAILED);

        return failedList.size();
    }

    /**
     * 배치 처리
     * - 트랜잭션 분리로 DB 커넥션 점유 시간 최소화
     * - EventListener와 동일한 방식으로 fcmSent 낙관적 설정
     */
    protected void processBatch(List<Notification> notifications, NotificationStatus fromStatus) {
        // T1: 권한 획득 + fcmSent=true 설정
        List<Notification> acquiredList = acquireBatchLockWithFcmFlag(notifications, fromStatus);

        if (acquiredList.isEmpty()) {
            log.debug("All notifications already being processed");
            return;
        }

        log.info("Acquired {} notifications for batch processing", acquiredList.size());

        try {
            // FCM 발송 (트랜잭션 밖)
            Map<Long, Boolean> results = fcmNotificationSender.sendBatch(acquiredList);

            // T2: 결과 업데이트
            updateBatchResults(acquiredList, results, fromStatus);

        } catch (Exception e) {
            log.error("Failed to process batch notifications", e);
            // 배치 전체 실패 시 모든 알림을 FAILED로 처리
            handleBatchFailure(acquiredList);
        }
    }

    /**
     * T1: 배치 권한 획득 + fcmSent=true 설정 (EventListener와 동일한 방식)
     * - PENDING → SENDING & fcmSent=true (TransactionHelper 사용)
     * - FAILED → SENDING & fcmSent=true & retry_count++ (TransactionHelper 사용)
     * - TransactionHelper가 이미 REQUIRES_NEW 트랜잭션을 제공하므로 별도 @Transactional 불필요
     */
    protected List<Notification> acquireBatchLockWithFcmFlag(List<Notification> notifications, NotificationStatus fromStatus) {
        List<Notification> acquiredList = new ArrayList<>();

        for (Notification notification : notifications) {
            try {
                if (fromStatus == NotificationStatus.PENDING) {
                    // PENDING → SENDING & fcmSent=true (REQUIRES_NEW 트랜잭션)
                    transactionHelper.markAsSendingWithFcmFlag(notification.getId());
                    acquiredList.add(notification);
                    log.debug("Acquired PENDING notification: id={}", notification.getId());
                } else if (fromStatus == NotificationStatus.FAILED) {
                    // FAILED → SENDING & fcmSent=true & retry_count++ (REQUIRES_NEW 트랜잭션)
                    transactionHelper.markFailedAsSendingWithFcmFlag(notification.getId());
                    acquiredList.add(notification);
                    log.debug("Acquired FAILED notification: id={}, retryCount={} (now incrementing)",
                        notification.getId(), notification.getRetryCount());
                }
            } catch (IllegalStateException e) {
                // 이미 다른 스레드가 처리 중
                log.debug("Already being processed: id={}", notification.getId());
            }
        }

        return acquiredList;
    }

    /**
     * T2: 배치 결과 업데이트
     * - 성공: SENDING → SENT (fcmSent는 이미 true)
     * - 실패: SENDING → FAILED & fcmSent=false
     *   - PENDING에서 온 경우: retry_count 유지 (T1에서 증가 안 함)
     *   - FAILED에서 온 경우: retry_count 유지 (T1에서 이미 증가됨)
     * - TransactionHelper가 이미 REQUIRES_NEW 트랜잭션을 제공하므로 별도 @Transactional 불필요
     */
    protected void updateBatchResults(List<Notification> notifications, Map<Long, Boolean> results, NotificationStatus fromStatus) {
        for (Notification notification : notifications) {
            Long notificationId = notification.getId();
            Boolean success = results.get(notificationId);

            if (success != null && success) {
                // 성공: EventListener의 updateFinalStatus(true)와 동일 (REQUIRES_NEW 트랜잭션)
                boolean updated = transactionHelper.updateFinalStatus(notificationId, true);

                if (updated) {
                    // PENDING에서 온 경우: retry_count = 0 (증가 안 함)
                    // FAILED에서 온 경우: retry_count = 메모리값 + 1 (T1에서 증가)
                    int actualRetryCount = (fromStatus == NotificationStatus.FAILED)
                        ? notification.getRetryCount() + 1
                        : notification.getRetryCount();
                    log.info("✅ Notification sent successfully: id={}, retryCount={}",
                        notificationId, actualRetryCount);
                }
            } else {
                // 실패: EventListener의 updateFinalStatus(false)와 동일 (REQUIRES_NEW 트랜잭션)
                boolean updated = transactionHelper.updateFinalStatus(notificationId, false);

                if (updated) {
                    // PENDING에서 온 경우: retry_count = 0 (T1에서 증가 안 함)
                    // FAILED에서 온 경우: retry_count = 메모리값 + 1 (T1에서 증가)
                    int actualRetryCount = (fromStatus == NotificationStatus.FAILED)
                        ? notification.getRetryCount() + 1
                        : notification.getRetryCount();
                    log.warn("⚠️ Notification marked as FAILED: id={}, retryCount={}",
                        notificationId, actualRetryCount);
                }
            }
        }
    }

    /**
     * 배치 전체 실패 처리
     * - TransactionHelper가 이미 REQUIRES_NEW 트랜잭션을 제공하므로 별도 @Transactional 불필요
     */
    protected void handleBatchFailure(List<Notification> notifications) {
        for (Notification notification : notifications) {
            try {
                // REQUIRES_NEW 트랜잭션
                transactionHelper.markAsFailed(notification.getId());
                log.warn("⚠️ Batch failure - notification marked as FAILED: id={}",
                    notification.getId());
            } catch (Exception ex) {
                log.error("Failed to handle failure for notification: id={}",
                    notification.getId(), ex);
            }
        }
    }
}