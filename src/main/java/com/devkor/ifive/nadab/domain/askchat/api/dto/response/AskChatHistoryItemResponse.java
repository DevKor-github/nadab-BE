package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Ask Chat 히스토리 목록 항목")
public record AskChatHistoryItemResponse(
        @Schema(description = "채팅 세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "히스토리 카드 제목으로 사용할 첫 사용자 질문", example = "나는 어떤 사람이야?")
        String title,

        @Schema(description = "채팅 세션 상태", example = "ACTIVE")
        AskChatSessionStatus status,

        @Schema(description = "성공적으로 답변된 대화 횟수", example = "3")
        int answeredTurnCount,

        @Schema(description = "채팅 시작 시각")
        OffsetDateTime createdAt
) {
}
