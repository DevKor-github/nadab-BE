package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

@Schema(description = "물어보기 홈 응답")
public record AskChatHomeResponse(
        @Schema(description = "세션당 최대 대화 횟수", example = "15")
        int maxTurnCount,

        @Schema(description = "홈 화면에 표시할 잔여 대화 횟수. 활성 세션이 없으면 세션당 최대 대화 횟수와 같습니다.", example = "12")
        int remainingTurnCount,

        @Schema(description = "현재 이어갈 수 있는 활성 채팅 세션입니다. 활성 세션이 없으면 이 필드만 null입니다.", nullable = true)
        AskChatSessionResponse activeSession
) {

    public static AskChatHomeResponse from(Optional<AskChatSession> session, int maxTurnCount) {
        AskChatSessionResponse activeSession = session
                .map(value -> AskChatSessionResponse.from(value, maxTurnCount))
                .orElse(null);
        int remainingTurnCount = activeSession == null
                ? maxTurnCount
                : activeSession.remainingTurnCount();

        return new AskChatHomeResponse(
                maxTurnCount,
                remainingTurnCount,
                activeSession
        );
    }
}
