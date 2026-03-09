package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 관계 상태")
public enum RelationshipStatus {
    @Schema(description = "나 자신")
    SELF,

    @Schema(description = "관계 없음")
    NONE,

    @Schema(description = "이미 친구")
    FRIEND,

    @Schema(description = "내가 보낸 요청 (취소 가능)")
    REQUEST_SENT,

    @Schema(description = "상대가 보낸 요청 (수락/거절 가능)")
    REQUEST_RECEIVED
}