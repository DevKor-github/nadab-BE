package com.devkor.ifive.nadab.global.core.notification.message;

public record NotificationContent(
    String title, // FCM 푸시 알림 제목
    String body, // FCM 푸시 알림 본문
    String inboxMessage // 알림함에 표시될 메시지
) {
    public NotificationContent {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title은 필수입니다.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body는 필수입니다.");
        }
        if (inboxMessage == null || inboxMessage.isBlank()) {
            throw new IllegalArgumentException("inboxMessage는 필수입니다.");
        }
    }
}