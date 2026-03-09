package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "검색 결과 리스트 응답")
public record SearchAnswerEntryResponse(
        @Schema(description = "검색 결과 항목 리스트")
        List<AnswerEntrySummaryResponse> items,

        @Schema(description = "다음 페이지 커서 (null이면 마지막 페이지)", example = "2025-12-25")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext
) {
}