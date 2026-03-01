package com.devkor.ifive.nadab.domain.notification.api.dto.response;

import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "알림 설정 응답")
public record NotificationSettingResponse(
    @Schema(description = "알림 그룹", example = "ACTIVITY_REMINDER")
    NotificationGroup group,

    @Schema(description = "활성화 여부", example = "true")
    boolean enabled,

    @Schema(
        description = "일일 작성 알림 시간 (ACTIVITY_REMINDER 그룹인 경우만)",
        example = "20:00"
    )
    @JsonFormat(pattern = "HH:mm")
    LocalTime dailyWriteTime
) {
    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(
            setting.getGroup(),
            setting.isEnabled(),
            setting.getDailyWriteTime()
        );
    }
}