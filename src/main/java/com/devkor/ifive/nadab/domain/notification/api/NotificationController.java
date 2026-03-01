package com.devkor.ifive.nadab.domain.notification.api;

import com.devkor.ifive.nadab.domain.notification.api.dto.request.SendTestNotificationRequest;
import com.devkor.ifive.nadab.domain.notification.api.dto.response.NotificationListResponse;
import com.devkor.ifive.nadab.domain.notification.api.dto.response.UnreadCountResponse;
import com.devkor.ifive.nadab.domain.notification.application.NotificationCommandService;
import com.devkor.ifive.nadab.domain.notification.application.NotificationQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 API", description = "알림 관련 API")
@RestController
@RequestMapping("${api_prefix}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "알림 목록 조회",
        description = """
            사용자의 알림 목록을 커서 기반 페이지네이션으로 조회합니다. 최신순으로 정렬됩니다.

            - 첫 조회: cursor 없이 요청 (최근 20개)
            - 다음 페이지: 응답의 nextCursor를 사용하여 요청
            - 한 번에 20개씩 조회됩니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = NotificationListResponse.class), mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "- ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<NotificationListResponse>> getNotifications(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Long cursor
    ) {
        return ApiResponseEntity.ok(
            NotificationListResponse.from(notificationQueryService.getUserNotifications(principal.getId(), cursor))
        );
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "미읽음 알림 개수 조회",
        description = "사용자의 읽지 않은 알림 개수를 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = UnreadCountResponse.class), mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "- ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<UnreadCountResponse>> getUnreadCount(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        long unreadCount = notificationQueryService.getUnreadCount(principal.getId());
        UnreadCountResponse response = new UnreadCountResponse(unreadCount);
        return ApiResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음 상태로 변경합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "읽음 처리 성공",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "- ErrorCode: NOTIFICATION_ACCESS_FORBIDDEN - 다른 사용자의 알림에 접근할 수 없음",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "- ErrorCode: NOTIFICATION_NOT_FOUND - 알림을 찾을 수 없음",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<Void>> markAsRead(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long notificationId
    ) {
        notificationCommandService.markAsRead(principal.getId(), notificationId);
        return ApiResponseEntity.noContent();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "전체 알림 읽음 처리",
        description = "사용자의 모든 미읽음 알림을 읽음 상태로 변경합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "전체 읽음 처리 성공",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<Void>> markAllAsRead(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationCommandService.markAllAsRead(principal.getId());
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "알림 삭제",
        description = "특정 알림을 삭제합니다. 삭제된 알림은 목록에 표시되지 않습니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "알림 삭제 성공",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "- ErrorCode: NOTIFICATION_ACCESS_FORBIDDEN - 다른 사용자의 알림에 접근할 수 없음",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "- ErrorCode: NOTIFICATION_NOT_FOUND - 알림을 찾을 수 없음",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteNotification(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long notificationId
    ) {
        notificationCommandService.deleteNotification(principal.getId(), notificationId);
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "전체 알림 삭제",
        description = "사용자의 모든 알림을 삭제합니다. 삭제된 알림은 목록에 표시되지 않습니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "전체 알림 삭제 성공",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteAllNotifications(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationCommandService.deleteAllNotifications(principal.getId());
        return ApiResponseEntity.noContent();
    }

    @PostMapping("/test")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "테스트 알림 발송",
        description = "테스트용 푸시 알림을 발송합니다. 등록된 디바이스로 즉시 전송됩니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "테스트 알림 발송 성공",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "400",
                description = "- 요청 데이터 검증 실패",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (JWT 토큰 관련)",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "- ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음",
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<Void>> sendTestNotification(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody SendTestNotificationRequest request
    ) {
        notificationCommandService.sendTestNotification(
            principal.getId(),
            request.title(),
            request.body()
        );
        return ApiResponseEntity.noContent();
    }
}