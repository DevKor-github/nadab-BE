package com.devkor.ifive.nadab.domain.notification.application;

import com.devkor.ifive.nadab.domain.notification.application.event.NotificationCreatedEvent;
import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationSettingRepository;
import com.devkor.ifive.nadab.domain.notification.infra.FcmNotificationSender;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 Command 서비스
 * - 알림 생성 및 발송
 * - 알림 읽음 처리
 * - 테스트 알림 발송
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final FcmNotificationSender fcmNotificationSender;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 알림 생성 (PENDING 상태로 저장 + 이벤트 발행)
     * - 중복 체크 (idempotency_key)
     * - 트랜잭션 커밋 후 EventListener가 즉시 발송 시도 (99%)
     * - 실패 시 Scheduler가 Fallback 재시도 (1%)
     */
    public void sendNotification(
        Long userId,
        NotificationType type,
        String title,
        String body,
        String inboxMessage,
        String targetId,
        String idempotencyKey
    ) {
        // 1. 중복 체크
        if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.info("Duplicate notification ignored: key={}", idempotencyKey);
            return;
        }

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 탈퇴한 사용자는 알림 생성 안 함
        if (user.getDeletedAt() != null) {
            log.info("User is deleted, skip notification: userId={}", userId);
            return;
        }

        // 3. 알림 설정 체크
        NotificationGroup group = type.getGroup();
        NotificationSetting setting = notificationSettingRepository
            .findByUserAndGroup(user, group)
            .orElseGet(() -> {
                NotificationSetting newSetting = NotificationSetting.create(user, group);
                return notificationSettingRepository.save(newSetting);
            });

        if (!setting.isEnabled()) {
            // 알림 설정 OFF → 알림함에만 저장, FCM 발송 안 함
            Notification notification = Notification.create(
                user, type, title, body, inboxMessage, targetId, idempotencyKey
            );
            notification.markAsNotificationDisabled();
            notificationRepository.save(notification);

            log.info("Notification saved to inbox only (user disabled): userId={}, group={}, id={}, status=NOTIFICATION_DISABLED",
                userId, group, notification.getId());
            return;  // 이벤트 발행 안 함
        }

        // 4. 알림 저장 (PENDING 상태)
        Notification notification = Notification.create(
            user, type, title, body, inboxMessage, targetId, idempotencyKey
        );
        notificationRepository.save(notification);

        log.info("Notification created (PENDING): id={}, type={}, userId={}",
            notification.getId(), type, userId);

        // 5. 이벤트 발행 (트랜잭션 커밋 후 EventListener가 즉시 발송)
        eventPublisher.publishEvent(new NotificationCreatedEvent(notification.getId()));
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 검증
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN);
        }

        notification.markAsRead();

        // 다른 기기 뱃지 동기화
        syncBadgeCount(userId);
    }

    /**
     * 전체 읽음 처리
     */
    public void markAllAsRead(Long userId) {
        User user = userRepository.getReferenceById(userId);

        notificationRepository.markAllAsReadByUser(user);

        // 다른 기기 뱃지 동기화
        syncBadgeCount(userId);
    }

    /**
     * 알림 삭제
     */
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 검증
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN);
        }

        // 이미 삭제된 경우
        if (notification.getDeletedAt() != null) {
            return;
        }

        notification.softDelete();

        // 다른 기기 뱃지 동기화
        syncBadgeCount(userId);
    }

    /**
     * 전체 알림 삭제
     */
    public void deleteAllNotifications(Long userId) {
        User user = userRepository.getReferenceById(userId);

        notificationRepository.softDeleteAllByUser(user);

        // 다른 기기 뱃지 동기화
        syncBadgeCount(userId);
    }

    /**
     * 테스트 알림 발송
     */
    public void sendTestNotification(Long userId, String title, String body) {
        String idempotencyKey = String.format("%d_TEST_%d", userId, System.currentTimeMillis());

        sendNotification(
                userId,
                NotificationType.DAILY_WRITE_REMINDER,
                title,
                body,
                "테스트 알림",
                null,
                idempotencyKey
        );
    }

    /**
     * 다른 기기로 뱃지 개수 동기화 (Silent Badge Update)
     * - 비동기로 즉시 실행 (실시간 동기화)
     */
    private void syncBadgeCount(Long userId) {
        fcmNotificationSender.sendSilentBadgeUpdate(userId);
    }
}