package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Ask Chat 히스토리 목록 항목")
public record AskChatHistoryItemResponse(
        @Schema(description = "채팅 세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "히스토리 카드 제목으로 사용할 첫 사용자 질문", example = "나는 어떤 사람이야?")
        String title,

        @Schema(description = "해당 세션에서 가장 최근에 전송한 사용자 질문", example = "요즘 내가 놓치고 있는 감정은 뭐야?")
        String lastUserQuestion,

        @Schema(description = "히스토리 카드에 표시할 작성일", example = "2026-06-20")
        LocalDate createdDate,

        @Schema(description = "채팅 세션 상태", example = "ACTIVE")
        AskChatSessionStatus status,

        @Schema(description = "해당 세션의 마지막 메시지 생성 시각")
        OffsetDateTime lastMessageAt
) {
}
