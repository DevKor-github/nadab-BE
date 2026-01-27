package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "받은 친구 요청 목록 응답")
public record PendingFriendListResponse(
        @Schema(description = "총 요청 수", example = "3")
        int totalCount,

        @Schema(description = "친구 요청 목록 (최신순)")
        List<PendingFriendResponse> requests
) {
}