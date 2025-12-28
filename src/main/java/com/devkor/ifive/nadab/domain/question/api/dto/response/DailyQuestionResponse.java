package com.devkor.ifive.nadab.domain.question.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 질문 응답")
public record DailyQuestionResponse(
        @Schema(description = "질문 ID")
        Long questionId,

        @Schema(description = "관심 주제 코드")
        String interestCode,

        @Schema(description = "질문 텍스트")
        String questionText,

        @Schema(description = "공감 가이드 텍스트")
        String empathyGuide,

        @Schema(description = "힌트 가이드 텍스트")
        String hintGuide,

        @Schema(description = "도입 질문 가이드 텍스트")
        String leadingQuestionGuide,

        @Schema(description = "사용자가 오늘의 질문에 답변했는지 여부")
        boolean answered,

        @Schema(description = "사용자가 새로운 질문 받기를 했는지 여부")
        boolean rerollUsed
) {
}
