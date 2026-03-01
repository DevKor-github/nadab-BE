package com.devkor.ifive.nadab.domain.notification.core.entity;

/**
 * 알림 발송 상태
 * - PENDING: 발송 대기 (초기 상태)
 * - SENDING: 발송 중 (Scheduler가 처리 중)
 * - SENT: 발송 성공
 * - FAILED: 발송 실패 (재시도 대상)
 * - DEAD_LETTER: 최종 실패 (재시도 포기, 수동 처리 필요)
 * - NOTIFICATION_DISABLED: 사용자가 알림 받지 않음 설정 (알림함에만 저장)
 */
public enum NotificationStatus {
    PENDING,               // 발송 대기
    SENDING,               // 발송 중
    SENT,                  // 발송 성공
    FAILED,                // 발송 실패 (재시도 예정)
    DEAD_LETTER,           // 최종 실패 (재시도 포기)
    NOTIFICATION_DISABLED  // 사용자가 알림 받지 않음 설정 (알림함에만 저장)
}