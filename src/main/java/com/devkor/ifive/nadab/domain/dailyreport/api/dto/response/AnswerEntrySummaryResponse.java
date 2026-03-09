package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.dailyreport.application.helper.MatchedSnippetExtractor;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "검색 결과 항목 (요약)")
public record AnswerEntrySummaryResponse(
        @Schema(description = "답변 ID", example = "123")
        Long answerId,

        @Schema(description = "관심분야 코드 (PREFERENCE, EMOTION, ROUTINE, RELATIONSHIP, LOVE, VALUES)", example = "EMOTION")
        String interestCode,

        @Schema(description = "감정 코드 (리포트 생성 완료 시에만 제공, PENDING/FAILED 상태면 null)", example = "ACHIEVEMENT")
        String emotionCode,

        @Schema(description = "질문 내용", example = "오늘 가장 기뻤던 순간은?")
        String questionText,

        @Schema(description = "답변 미리보기 (키워드 포함 첫 문장 또는 답변 시작 문장, 20~100자, 문장 단위, 끝마침표 없음)", example = "친구와 함께한 시간이 정말 영광이었다")
        String matchedSnippet,

        @Schema(description = "답변 작성일", example = "2025-12-25")
        LocalDate answerDate
) {
    /**
     * 검색 키워드가 있는 경우 (키워드 포함 문장 추출)
     */
    public static AnswerEntrySummaryResponse from(SearchAnswerEntryDto dto, String keyword) {
        String snippet = MatchedSnippetExtractor.extract(dto.answerContent(), keyword);

        return new AnswerEntrySummaryResponse(
                dto.answerId(),
                dto.interestCode() != null ? dto.interestCode().name() : null,
                dto.emotionCode() != null ? dto.emotionCode().name() : null,
                dto.questionText(),
                snippet,
                dto.answerDate()
        );
    }

    /**
     * 검색 키워드가 없는 경우 (첫 문장 추출)
     */
    public static AnswerEntrySummaryResponse from(SearchAnswerEntryDto dto) {
        return from(dto, null);
    }
}