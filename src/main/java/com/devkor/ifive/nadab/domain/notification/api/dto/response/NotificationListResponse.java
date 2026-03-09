package com.devkor.ifive.nadab.domain.notification.api.dto.response;

import com.devkor.ifive.nadab.domain.notification.application.NotificationQueryService.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "알림 목록 응답 (커서 기반 페이지네이션, 20개씩)")
public record NotificationListResponse(
    @Schema(description = "알림 목록 (최신순, 최대 20개)")
    List<NotificationResponse> notifications,

    @Schema(description = "다음 페이지 커서 (마지막 알림 ID, null이면 마지막 페이지)", example = "123")
    Long nextCursor,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {
    public static NotificationListResponse from(PageResult pageResult) {
        List<NotificationResponse> responses = pageResult.content().stream()
            .map(NotificationResponse::from)
            .toList();

        return new NotificationListResponse(
            responses,
            pageResult.nextCursor(),
            pageResult.hasNext()
        );
    }
}