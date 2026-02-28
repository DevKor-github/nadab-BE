package com.devkor.ifive.nadab.domain.notification.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "inbox_message", nullable = false, length = 200)
    private String inboxMessage;

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 200)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private NotificationStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "fcm_sent", nullable = false)
    private boolean fcmSent = false;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;


    public static Notification create(
        User user,
        NotificationType type,
        String title,
        String body,
        String inboxMessage,
        String targetId,
        String idempotencyKey
    ) {
        Notification notification = new Notification();
        notification.user = user;
        notification.type = type;
        notification.title = title;
        notification.body = body;
        notification.inboxMessage = inboxMessage;
        notification.targetId = targetId;
        notification.idempotencyKey = idempotencyKey;
        notification.status = NotificationStatus.PENDING;
        return notification;
    }

    public void markAsNotificationDisabled() {
        this.status = NotificationStatus.NOTIFICATION_DISABLED;
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = OffsetDateTime.now();
    }
}