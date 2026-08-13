package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 남은 메시지 횟수 응답")
public record AskChatRemainingMessageCountResponse(
        @Schema(description = "사용 가능한 남은 메시지 횟수", example = "9")
        int remainingMessageCount
) {
}
