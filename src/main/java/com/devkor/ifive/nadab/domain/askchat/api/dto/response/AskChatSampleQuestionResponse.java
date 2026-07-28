package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 홈 예시 질문 응답")
public record AskChatSampleQuestionResponse(
        @Schema(description = "예시 질문 ID", example = "1")
        Long id,

        @Schema(description = "예시 질문 주제 코드. interests.code 값을 사용합니다.", example = "VALUES")
        String category,

        @Schema(description = "예시 질문 내용", example = "나는 어떤 사람이야?")
        String question
) {

    public static AskChatSampleQuestionResponse from(AskChatSampleQuestion sampleQuestion) {
        return new AskChatSampleQuestionResponse(
                sampleQuestion.getId(),
                sampleQuestion.getInterestCode().name(),
                sampleQuestion.getQuestion()
        );
    }
}
