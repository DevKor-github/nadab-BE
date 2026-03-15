package com.devkor.ifive.nadab.domain.question.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 질문 응답")
public record DailyQuestionResponse(
        @Schema(description = "질문 ID")
        Long questionId,

        @Schema(description = "관심 주제 코드", example = "PREFERENCE")
        String interestCode,

        @Schema(description = "질문 텍스트", example = "요즘 자주 찾는 색깔은 무엇인가요?")
        String questionText,

        @Schema(description = "공감 가이드 텍스트", example = "색깔 하나로 기분이 달라질 때가 있어요.")
        String empathyGuide,

        @Schema(description = "힌트 가이드 텍스트", example = "지금 입은 옷이나 주변 소품을 보세요.")
        String hintGuide,

        @Schema(description = "도입 질문 가이드 텍스트", example = "그 색을 보면 어떤 기분이 드나요?")
        String leadingQuestionGuide,

        @Schema(description = "사용자가 오늘의 질문에 답변했는지 여부")
        boolean answered,

        @Schema(description = "사용자가 새로운 질문 받기를 했는지 여부")
        boolean rerollUsed
) {
}
