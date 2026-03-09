package com.devkor.ifive.nadab.domain.notification.core.dto;

import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

/**
 * FCM 발송용 내부 DTO
 */
@Getter
@Builder
public class NotificationMessageDto {

    private NotificationType type;
    private String title;
    private String body;
    private String inboxMessage;
    private String targetId;
    private int unreadCount;  // 읽지 않은 알림 개수 (뱃지용)

    public static NotificationMessageDto from(Notification notification, int unreadCount) {
        return NotificationMessageDto.builder()
            .type(notification.getType())
            .title(notification.getTitle())
            .body(notification.getBody())
            .inboxMessage(notification.getInboxMessage())
            .targetId(notification.getTargetId())
            .unreadCount(unreadCount)
            .build();
    }
}