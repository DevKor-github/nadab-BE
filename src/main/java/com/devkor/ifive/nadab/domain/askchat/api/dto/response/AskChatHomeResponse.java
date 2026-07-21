package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "물어보기 홈 응답")
public record AskChatHomeResponse(
        @Schema(description = "세션당 최대 대화 횟수", example = "15")
        int maxTurnCount,

        @Schema(description = "홈 화면에서 이어갈 수 있는 최근 채팅 세션 목록. USER 메시지가 1개 이상 있는 세션만 포함합니다.")
        List<AskChatHistoryItemResponse> recentSessions,

        @Schema(description = "최근 채팅 세션 목록이 비어 있는지 여부", example = "false")
        boolean recentSessionsEmpty
) {

    public static AskChatHomeResponse of(
            int maxTurnCount,
            List<AskChatHistoryItemResponse> recentSessions
    ) {
        return new AskChatHomeResponse(
                maxTurnCount,
                recentSessions,
                recentSessions.isEmpty()
        );
    }
}
