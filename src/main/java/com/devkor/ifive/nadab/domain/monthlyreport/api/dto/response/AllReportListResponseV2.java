package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "전체 리포트 목록 페이지 응답")
public record AllReportListResponseV2(
        @Schema(description = "현재 페이지의 리포트 목록")
        List<AllReportItemResponseV2> items,
        @Schema(description = "전체 리포트 개수", example = "43")
        int totalCount,
        @Schema(description = "현재 페이지 번호(1부터 시작)", example = "1")
        int currentPage,
        @Schema(description = "페이지 크기", example = "7")
        int pageSize,
        @Schema(description = "전체 페이지 수", example = "7")
        int totalPages,
        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
