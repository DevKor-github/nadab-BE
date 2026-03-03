package com.devkor.ifive.nadab.domain.notification.application.event;

import com.devkor.ifive.nadab.domain.notification.application.helper.NotificationTransactionHelper;
import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import com.devkor.ifive.nadab.domain.notification.infra.FcmNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 이벤트 리스너
 * - 알림 생성 이벤트를 받아서 즉시 FCM 발송
 * - 트랜잭션 최적화 + 중복 발송 완전 방지
 * - 모든 @Modifying 쿼리는 TransactionHelper로 위임 (명시적 트랜잭션 관리)
 *
 * 트랜잭션 구조:
 * T1: 알림 생성 (PENDING)
 *   ↓ AFTER_COMMIT
 * EventListener:
 *   T2: PENDING → SENDING & fcmSent = true (낙관적 설정, TransactionHelper)
 *   FCM 발송 (트랜잭션 밖)
 *   T3: SENDING → SENT (fcmSent = true 유지, TransactionHelper)
 *       또는 SENDING → FAILED & fcmSent = false (복구, TransactionHelper)
 *
 * 중복 발송 방지:
 * - fcmSent를 FCM 발송 전에 미리 true로 설정
 * - FCM 성공 후 T3 실패 시 → fcmSent = true로 보호
 * - RecoveryScheduler가 fcmSent = true 확인 → SENT로만 변경
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final FcmNotificationSender fcmSender;
    private final NotificationTransactionHelper transactionHelper;

    /**
     * 알림 생성 이벤트 처리
     * - AFTER_COMMIT: 알림 생성 트랜잭션이 커밋된 후 실행
     * - @Async: 비동기로 즉시 발송 (알림 생성 API 응답 속도에 영향 없음)
     */
    @Async("fcmTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        Long notificationId = event.getNotificationId();

        try {
            // T2: PENDING → SENDING & fcmSent = true (낙관적 설정)
            transactionHelper.markAsSendingWithFcmFlag(notificationId);

            // Notification 조회 (트랜잭션 밖)
            Notification notification = notificationRepository.findByIdWithUser(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

            // FCM 발송 (트랜잭션 밖)
            boolean fcmSuccess = fcmSender.sendInternal(notification);

            // T3: FCM 발송 결과에 따른 최종 상태 업데이트 (별도 트랜잭션)
            boolean updated = transactionHelper.updateFinalStatus(notificationId, fcmSuccess);

            // UPDATE 실패 시 처리
            if (!updated) {
                if (fcmSuccess) {
                    // FCM 발송 성공 → SENT 마킹 실패
                    // RecoveryScheduler가 fcmSent=true 확인 후 SENT로 변경
                    log.warn("FCM sent but status update failed, RecoveryScheduler will handle: id={}", notificationId);
                } else {
                    // FCM 발송 실패 → FAILED 마킹도 실패
                    // 예외 발생 → catch 블록에서 markAsFailed() 재시도
                    log.error("Failed to mark as FAILED, throwing exception for recovery: id={}", notificationId);
                    throw new IllegalStateException("Failed to update notification to FAILED status");
                }
            }

        } catch (Exception e) {
            log.error("Failed to handle notification created event: id={}, error={}",
                notificationId, e.getMessage(), e);

            // 예외 발생 시 FAILED로 복구 (별도 트랜잭션)
            transactionHelper.markAsFailed(notificationId);
        }
    }
}