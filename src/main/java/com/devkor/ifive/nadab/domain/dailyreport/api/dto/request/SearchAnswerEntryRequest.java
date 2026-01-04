package com.devkor.ifive.nadab.domain.dailyreport.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "답변 검색 요청")
public record SearchAnswerEntryRequest(
        @Schema(description = "검색 키워드 (질문 또는 답변 내용)", example = "영광")
        @Size(min = 1, max = 100, message = "검색 키워드는 1~100자입니다")
        String keyword,

        @Schema(description = "감정 코드 (JOY, PLEASURE, SADNESS, ANGER, REGRET, FRUSTRATION, GROWTH, ETC)", example = "JOY")
        String emotionCode,

        @Schema(description = "다음 페이지 커서 (형식: date)", example = "2025-12-25")
        String cursor
) {
}