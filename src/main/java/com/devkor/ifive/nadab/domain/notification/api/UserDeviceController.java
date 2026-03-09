package com.devkor.ifive.nadab.domain.notification.api;

import com.devkor.ifive.nadab.domain.notification.api.dto.request.RegisterDeviceRequest;
import com.devkor.ifive.nadab.domain.notification.api.dto.response.RegisterDeviceResponse;
import com.devkor.ifive.nadab.domain.notification.application.UserDeviceCommandService;
import com.devkor.ifive.nadab.domain.notification.core.entity.DevicePlatform;
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

@Tag(name = "디바이스 API", description = "FCM 디바이스 토큰 관리 API")
@RestController
@RequestMapping("${api_prefix}/notifications/tokens")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceCommandService userDeviceCommandService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "FCM 토큰 등록",
        description = """
            FCM 토큰을 등록합니다. 로그인, 회원가입 또는 FCM 토큰 갱신 시 호출해야 합니다.

            호출 시점:
            - 로그인/회원가입 완료 후
            - FCM 토큰 갱신 시 (onTokenRefresh 콜백)
            - 앱 재시작 시 (필요한 경우)

            동작:
            - 동일한 디바이스 ID가 이미 있는 경우 → 토큰만 업데이트
            - 새로운 디바이스인 경우 → 신규 등록
            - 응답의 isNewDevice 필드로 구분 가능

            참고:
            - 디바이스 ID: 디바이스를 식별할 수 있는 고유 값 (UUID 권장)
            - 플랫폼: IOS 또는 ANDROID
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "FCM 토큰 등록 성공",
                content = @Content(schema = @Schema(implementation = RegisterDeviceResponse.class), mediaType = "application/json")
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
    public ResponseEntity<ApiResponseDto<RegisterDeviceResponse>> registerDevice(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody RegisterDeviceRequest request
    ) {
        boolean isNewDevice = userDeviceCommandService.registerDevice(
            principal.getId(),
            request.fcmToken(),
            request.deviceId(),
            request.platform()
        );

        RegisterDeviceResponse response = new RegisterDeviceResponse(request.deviceId(), request.platform(), isNewDevice);
        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/{deviceId}/{platform}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "FCM 토큰 삭제",
        description = """
            등록된 FCM 토큰을 삭제합니다. 로그아웃 시 호출합니다.

            로그아웃 시 아래 순서대로 처리하는 것을 권장합니다.

            1. FCM 토큰 무효화
               - iOS: Messaging.messaging().deleteToken()
               - Android: FirebaseMessaging.getInstance().deleteToken()

            2. 서버 API 호출 (본 API)
               - 1단계 완료 후 호출
               - 네트워크 실패 시에도 1단계로 인해 토큰이 무효화되어 개인정보가 보호됩니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "FCM 토큰 삭제 성공",
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
                description = """
                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                    - ErrorCode: DEVICE_NOT_FOUND - 디바이스를 찾을 수 없음
                    """,
                content = @Content
            )
        }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteDevice(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String deviceId,
        @PathVariable DevicePlatform platform
    ) {
        userDeviceCommandService.deleteDevice(principal.getId(), deviceId, platform);
        return ApiResponseEntity.noContent();
    }
}