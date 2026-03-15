package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AnswerDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "답변 상세 조회 응답")
public record AnswerDetailResponse(

        @Schema(description = "질문 내용", example = "오늘 가장 기뻤던 순간은?")
        String questionText,

        @Schema(description = "질문 카테고리 (관심분야 코드)", example = "EMOTION")
        String interestCode,

        @Schema(description = "답변 작성일", example = "2025-12-25")
        LocalDate answerDate,

        @Schema(description = "나의 답변")
        String answer,

        @Schema(description = "리포트 내용")
        String content,

        @Schema(description = "리포트 감정 상태", example = "ACHIEVEMENT")
        String emotion
) {
    public static AnswerDetailResponse from(AnswerDetailDto dto) {
        return new AnswerDetailResponse(
                dto.questionText(),
                dto.interestCode() != null ? dto.interestCode().name() : null,
                dto.answerDate(),
                dto.answerContent(),
                dto.reportContent(),
                dto.emotionCode() != null ? dto.emotionCode().name() : null
        );
    }
}