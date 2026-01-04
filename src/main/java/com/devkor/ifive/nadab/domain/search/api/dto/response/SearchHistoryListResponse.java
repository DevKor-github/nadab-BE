package com.devkor.ifive.nadab.domain.search.api.dto.response;

import com.devkor.ifive.nadab.domain.search.core.entity.SearchHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "최근 검색어 리스트 응답", example = """
        {
          "histories": [
            {"id": 123, "keyword": "영광"},
            {"id": 122, "keyword": "고통"},
            {"id": 121, "keyword": "행복"},
            ...
          ]
        }
        """)
public record SearchHistoryListResponse(
        @Schema(description = "검색어 리스트 (최신순 10개)")
        List<SearchHistoryResponse> histories
) {
    public static SearchHistoryListResponse from(List<SearchHistory> histories) {
        List<SearchHistoryResponse> items = histories.stream()
                .map(SearchHistoryResponse::from)
                .toList();
        return new SearchHistoryListResponse(items);
    }
}