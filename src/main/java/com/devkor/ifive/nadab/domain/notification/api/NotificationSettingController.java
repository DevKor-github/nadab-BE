package com.devkor.ifive.nadab.domain.notification.api;

import com.devkor.ifive.nadab.domain.notification.api.dto.request.UpdateNotificationSettingRequest;
import com.devkor.ifive.nadab.domain.notification.api.dto.response.NotificationSettingResponse;
import com.devkor.ifive.nadab.domain.notification.application.NotificationSettingService;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
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
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "알림 설정 API", description = "알림 설정 관리 API")
@RestController
@RequestMapping("${api_prefix}/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "알림 설정 조회",
        description = """
            사용자의 알림 설정을 조회합니다. 그룹별로 3개의 설정이 반환됩니다.

            - ACTIVITY_REMINDER: 활동 리마인드 알림 (작성 알림 시간 설정 가능)
            - REPORT: 리포트 알림
            - SOCIAL: 소셜 알림 (친구 요청, 수락 등)

            설정이 없는 경우 기본값으로 생성되어 반환됩니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = NotificationSettingResponse.class), mediaType = "application/json")
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
    public ResponseEntity<ApiResponseDto<List<NotificationSettingResponse>>> getSettings(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<NotificationSetting> settings = notificationSettingService.getSettings(principal.getId());
        List<NotificationSettingResponse> response = settings.stream()
            .map(NotificationSettingResponse::from)
            .toList();
        return ApiResponseEntity.ok(response);
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "알림 설정 수정",
        description = """
            알림 설정을 수정합니다. 배열로 여러 그룹을 한 번에 수정할 수 있습니다.

            - 1개 그룹만 수정: [{ "group": "ACTIVITY_REMINDER", "enabled": true }]
            - 2개 그룹 수정: [{ "group": "ACTIVITY_REMINDER", ... }, { "group": "REPORT", ... }]
            - 전체 수정: 3개 객체 전송

            각 객체:
            - group: 알림 그룹 (필수)
            - enabled: 해당 그룹의 알림을 켜거나 끕니다 (필수)
            - dailyWriteTime: ACTIVITY_REMINDER 그룹인 경우만 설정 가능 (선택, 형식: HH:mm)
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "설정 수정 성공",
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
    public ResponseEntity<ApiResponseDto<Void>> updateSettings(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @NotEmpty(message = "알림 설정 목록은 비어있을 수 없습니다") @RequestBody List<UpdateNotificationSettingRequest> requests
    ) {
        notificationSettingService.updateSettings(principal.getId(), requests);
        return ApiResponseEntity.noContent();
    }
}