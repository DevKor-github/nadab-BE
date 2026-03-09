package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 관계 응답")
public record FriendshipResponse(
        @Schema(description = "친구 관계 ID", example = "123")
        Long friendshipId
) {
}