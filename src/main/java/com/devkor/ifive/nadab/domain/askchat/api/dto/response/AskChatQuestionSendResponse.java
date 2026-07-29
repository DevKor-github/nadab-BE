package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "물어보기 질문 전송 응답")
public record AskChatQuestionSendResponse(
        @Schema(description = "질문이 저장된 채팅 세션")
        AskChatSessionResponse session,

        @Schema(description = "저장된 사용자 질문 메시지")
        AskChatMessageResponse userMessage,

        @Schema(description = "저장된 AI 답변 메시지. 생성 실패 시 채팅 말풍선으로 표시하지 않도록 null로 반환", nullable = true)
        AskChatMessageResponse assistantMessage,

        @Schema(description = "답변 생성 성공/실패 상태. 실패 시 프론트에서는 이 값을 기준으로 모달/토스트를 표시합니다.")
        AskChatAnswerGenerationResponse answerGeneration,

        @Schema(description = "답변 생성 처리 이후 사용 가능한 남은 메시지 횟수. 성공 시에는 차감 이후 값, 실패 시에는 환불 이후 값입니다.", example = "8")
        int remainingMessageCount,

        @Schema(description = "AI가 제안한 후속 추천 질문. 생성 실패 시 빈 배열")
        List<String> followUpQuestions
) {

    public AskChatQuestionSendResponse {
        followUpQuestions = followUpQuestions == null ? List.of() : List.copyOf(followUpQuestions);
    }
}
