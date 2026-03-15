package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "친구 목록 응답")
public record FriendListResponse(
        @Schema(description = "총 친구 수", example = "5")
        int totalCount,

        @Schema(description = "친구 목록")
        List<FriendResponse> friends
) {
}