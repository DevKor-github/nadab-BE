package com.devkor.ifive.nadab.domain.notification.api.dto.response;

import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "알림 응답")
public record NotificationResponse(
    @Schema(description = "알림 ID", example = "1")
    Long id,

    @Schema(description = "알림 타입", example = "DAILY_WRITE_REMINDER")
    NotificationType type,

    @Schema(description = "알림 제목", example = "오늘의 질문에 답변해주세요")
    String title,

    @Schema(description = "알림 본문", example = "아직 답변하지 않은 질문이 있어요")
    String body,

    @Schema(description = "알림함 메시지", example = "오늘의 질문에 답변해보세요!")
    String inboxMessage,

    @Schema(description = "대상 리소스 ID", example = "123")
    String targetId,

    @Schema(description = "읽음 여부", example = "false")
    boolean isRead,

    @Schema(description = "생성 시각", example = "2025-01-15T09:00:00+09:00")
    OffsetDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getBody(),
            notification.getInboxMessage(),
            notification.getTargetId(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}