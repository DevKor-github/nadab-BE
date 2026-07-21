package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "물어보기 질문 전송 응답")
public record AskChatQuestionSendResponse(
        @Schema(description = "질문이 저장된 채팅 세션")
        AskChatSessionResponse session,

        @Schema(description = "저장된 사용자 질문 메시지")
        AskChatMessageResponse userMessage,

        @Schema(description = "저장된 AI 답변 메시지. 생성 실패 시 status=FAILED로 반환")
        AskChatMessageResponse assistantMessage,

        @Schema(description = "AI가 제안한 후속 추천 질문. 생성 실패 시 빈 배열")
        List<String> followUpQuestions
) {

    public AskChatQuestionSendResponse {
        followUpQuestions = followUpQuestions == null ? List.of() : List.copyOf(followUpQuestions);
    }
}
