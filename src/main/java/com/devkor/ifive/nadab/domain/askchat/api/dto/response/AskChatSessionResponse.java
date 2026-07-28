package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 채팅 세션 응답")
public record AskChatSessionResponse(
        @Schema(description = "채팅 세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "채팅 세션 상태", example = "ACTIVE")
        AskChatSessionStatus status,

        @Schema(description = "성공적으로 답변받은 턴 수", example = "3")
        int answeredTurnCount,

        @Schema(description = "세션당 최대 대화 횟수", example = "15")
        int maxTurnCount,

        @Schema(description = "현재 세션의 잔여 대화 횟수", example = "12")
        int remainingTurnCount
) {

    public static AskChatSessionResponse from(AskChatSession session, int maxTurnCount) {
        int remainingTurnCount = Math.max(0, maxTurnCount - session.getAnsweredTurnCount());

        return new AskChatSessionResponse(
                session.getId(),
                session.getStatus(),
                session.getAnsweredTurnCount(),
                maxTurnCount,
                remainingTurnCount
        );
    }
}
