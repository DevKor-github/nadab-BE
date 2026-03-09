package com.devkor.ifive.nadab.domain.notification.api.dto.request;

import com.devkor.ifive.nadab.domain.notification.core.entity.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FCM 토큰 등록 요청")
public record RegisterDeviceRequest(
    @Schema(description = "Firebase Cloud Messaging 토큰", example = "eFg12HiJKlMnOpQrStUvWxYz:APA91bHb6sT...")
    @NotBlank(message = "FCM 토큰은 필수입니다")
    String fcmToken,

    @Schema(description = "디바이스를 식별할 수 있는 고유 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "디바이스 ID는 필수입니다")
    String deviceId,

    @Schema(description = "디바이스 플랫폼 (IOS 또는 ANDROID)", example = "IOS")
    @NotNull(message = "플랫폼은 필수입니다")
    DevicePlatform platform
) {
}