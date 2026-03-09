package com.devkor.ifive.nadab.domain.notification.core.entity;

import lombok.Getter;

/**
 * 알림 그룹
 * - 여러 알림 타입을 묶어서 on/off 제어
 */
@Getter
public enum NotificationGroup {
    ACTIVITY_REMINDER("활동 리마인드 알림"),
    REPORT("리포트 알림"),
    SOCIAL("소셜 알림");

    private final String description;

    NotificationGroup(String description) {
        this.description = description;
    }
}