package com.devkor.ifive.nadab.domain.notification.api.dto.request;

import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Schema(description = "알림 설정 업데이트 요청")
public record UpdateNotificationSettingRequest(
    @Schema(description = "알림 그룹 (ACTIVITY_REMINDER, REPORT, SOCIAL)", example = "ACTIVITY_REMINDER")
    @NotNull(message = "알림 그룹은 필수입니다")
    NotificationGroup group,

    @Schema(description = "알림 활성화 여부 (필수)", example = "true")
    @NotNull(message = "알림 활성화 여부는 필수입니다")
    Boolean enabled,

    @Schema(description = "일일 작성 알림 시간 (ACTIVITY_REMINDER 그룹인 경우만 설정 가능)", example = "20:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime dailyWriteTime
) {
}