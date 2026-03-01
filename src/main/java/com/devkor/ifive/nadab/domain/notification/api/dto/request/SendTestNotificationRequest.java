package com.devkor.ifive.nadab.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "테스트 알림 발송 요청")
public record SendTestNotificationRequest(
    @Schema(
        description = "알림 제목",
        example = "테스트 알림입니다"
    )
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    String title,

    @Schema(
        description = "알림 본문",
        example = "이것은 푸시 알림 테스트 메시지입니다."
    )
    @NotBlank(message = "본문은 필수입니다")
    @Size(max = 500, message = "본문은 500자 이하여야 합니다")
    String body
) {
}