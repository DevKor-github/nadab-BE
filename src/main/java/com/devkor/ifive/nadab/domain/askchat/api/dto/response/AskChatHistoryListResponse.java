package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ask Chat 히스토리 목록 응답")
public record AskChatHistoryListResponse(
        @Schema(description = "히스토리 목록")
        List<AskChatHistoryItemResponse> histories,

        @Schema(description = "전체 히스토리 수", example = "12")
        long totalCount,

        @Schema(description = "현재 페이지 번호(1부터 시작)", example = "1")
        int currentPage,

        @Schema(description = "페이지 크기", example = "20")
        int pageSize,

        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext
) {
}
