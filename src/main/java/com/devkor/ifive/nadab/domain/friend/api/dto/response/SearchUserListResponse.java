package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "유저 검색 결과 응답")
public record SearchUserListResponse(
        @Schema(description = "나한테 친구 요청 보낸 사람들 (최신순)")
        List<SearchUserResponse> pendingRequests,

        @Schema(description = "검색 결과 (관련도순)")
        List<SearchUserResponse> searchResults,

        @Schema(description = "다음 페이지 커서 (null이면 마지막 페이지)", example = "모래가나")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext
) {
}