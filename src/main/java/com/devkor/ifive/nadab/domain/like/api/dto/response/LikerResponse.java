package com.devkor.ifive.nadab.domain.like.api.dto.response;

import com.devkor.ifive.nadab.domain.friend.api.dto.response.RelationshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 누른 사용자")
public record LikerResponse(

        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "닉네임", example = "모래")
        String nickname,

        @Schema(description = "친구 관계 ID (친구 신청 취소·삭제 시 사용, NONE 상태일 땐 null)", example = "123")
        Long friendshipId,

        @Schema(description = "친구 관계 상태 (FRIEND: 친구 삭제·차단 버튼 표시, REQUEST_SENT: 신청 취소 버튼 표시, REQUEST_RECEIVED: 수락·거절 버튼 표시, NONE: 친구 신청 버튼 표시)")
        RelationshipStatus relationshipStatus
) {
}