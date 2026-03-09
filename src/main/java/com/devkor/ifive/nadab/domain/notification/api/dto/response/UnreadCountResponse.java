package com.devkor.ifive.nadab.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미읽음 알림 개수 응답")
public record UnreadCountResponse(
    @Schema(description = "미읽음 알림 개수", example = "5")
    long unreadCount
) {
}