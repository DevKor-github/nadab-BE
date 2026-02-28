package com.devkor.ifive.nadab.domain.notification.api.dto.response;

import com.devkor.ifive.nadab.domain.notification.core.entity.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 토큰 등록 응답")
public record RegisterDeviceResponse(
    @Schema(description = "디바이스 고유 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String deviceId,

    @Schema(description = "디바이스 플랫폼", example = "IOS")
    DevicePlatform platform,

    @Schema(description = "새로 등록된 디바이스인지 여부 (true: 새 등록, false: 기존 토큰 업데이트)", example = "true")
    boolean isNewDevice
) {
}