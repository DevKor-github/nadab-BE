package com.devkor.ifive.nadab.domain.notification.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 생성 이벤트
 * - 트랜잭션 커밋 후 FCM 즉시 발송 트리거
 * - 실패 시 Scheduler가 Fallback으로 재시도
 */
@Getter
@RequiredArgsConstructor
public class NotificationCreatedEvent {

    private final Long notificationId;
}