package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 홈 응답")
public record AskChatHomeResponse(
        @Schema(description = "세션당 최대 대화 횟수", example = "15")
        int maxTurnCount
) {

    public static AskChatHomeResponse of(int maxTurnCount) {
        return new AskChatHomeResponse(maxTurnCount);
    }
}
