package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Ask Chat 히스토리 상세 응답")
public record AskChatHistoryDetailResponse(
        @Schema(description = "채팅 세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "채팅 세션 상태", example = "ENDED")
        AskChatSessionStatus status,

        @Schema(description = "성공적으로 답변된 대화 횟수", example = "5")
        int answeredTurnCount,

        @Schema(description = "해당 세션에 새 질문을 입력할 수 없는지 여부", example = "false")
        boolean readOnly,

        @Schema(description = "채팅 시작 시각")
        OffsetDateTime createdAt,

        @Schema(description = "채팅 종료 시각")
        OffsetDateTime endedAt,

        @Schema(description = "시간순 메시지 목록")
        List<AskChatMessageResponse> messages
) {
}
