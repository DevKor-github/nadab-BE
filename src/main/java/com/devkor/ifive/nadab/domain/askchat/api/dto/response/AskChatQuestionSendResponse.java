package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 질문 전송 응답")
public record AskChatQuestionSendResponse(
        @Schema(description = "질문이 저장된 채팅 세션")
        AskChatSessionResponse session,

        @Schema(description = "저장된 사용자 질문 메시지")
        AskChatMessageResponse userMessage
) {
}
