package com.devkor.ifive.nadab.domain.search.api.dto.response;

import com.devkor.ifive.nadab.domain.search.core.entity.SearchHistory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검색어 항목")
public record SearchHistoryResponse(
        @Schema(description = "검색어 ID", example = "789")
        Long id,

        @Schema(description = "검색 키워드", example = "영광")
        String keyword
) {
    public static SearchHistoryResponse from(SearchHistory history) {
        return new SearchHistoryResponse(
                history.getId(),
                history.getKeyword()
        );
    }
}