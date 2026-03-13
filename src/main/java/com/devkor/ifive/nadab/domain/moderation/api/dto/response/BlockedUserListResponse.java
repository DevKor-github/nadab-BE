package com.devkor.ifive.nadab.domain.moderation.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "차단 사용자 목록 응답")
public record BlockedUserListResponse(
        @Schema(description = "총 차단 사용자 수", example = "2")
        int totalCount,

        @Schema(description = "차단 사용자 목록")
        List<BlockedUserResponse> blockedUsers
) {
}
