package com.devkor.ifive.nadab.domain.notification.core.entity;

import lombok.Getter;

/**
 * 알림 타입
 * - 각 타입은 특정 그룹에 속함
 */
@Getter
public enum NotificationType {
    // 활동 리마인드 알림
    DAILY_WRITE_REMINDER("지정 시간 작성 알림", NotificationGroup.ACTIVITY_REMINDER),
    INACTIVE_USER_REMINDER("미답변 질문 재알림", NotificationGroup.ACTIVITY_REMINDER),

    // 리포트 알림
    WEEKLY_REPORT_COMPLETED("주간 리포트 완성", NotificationGroup.REPORT),
    MONTHLY_REPORT_COMPLETED("월간 리포트 완성", NotificationGroup.REPORT),
    TYPE_REPORT_COMPLETED("유형 리포트 완성", NotificationGroup.REPORT),
    WEEKLY_REPORT_AVAILABLE("주간 리포트 제작 가능", NotificationGroup.REPORT),
    MONTHLY_REPORT_AVAILABLE("월간 리포트 제작 가능", NotificationGroup.REPORT),
    TYPE_REPORT_AVAILABLE("유형 리포트 제작 가능", NotificationGroup.REPORT),

    // 소셜 알림
    FRIEND_REQUEST_RECEIVED("친구 요청", NotificationGroup.SOCIAL),
    FRIEND_REQUEST_ACCEPTED("친구 수락", NotificationGroup.SOCIAL);

    private final String description;
    private final NotificationGroup group;

    NotificationType(String description, NotificationGroup group) {
        this.description = description;
        this.group = group;
    }
}