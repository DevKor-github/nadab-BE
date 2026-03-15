package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "최근 친구 검색 기록 리스트 응답")
public record SearchHistoryListResponse(
        @Schema(description = "검색 기록 리스트 (최신순, 최대 5개)")
        List<SearchHistoryResponse> histories
) {
}