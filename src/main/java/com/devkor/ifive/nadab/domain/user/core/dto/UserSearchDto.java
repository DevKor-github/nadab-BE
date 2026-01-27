package com.devkor.ifive.nadab.domain.user.core.dto;

import com.devkor.ifive.nadab.domain.friend.api.dto.response.RelationshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;

/**
 * 유저 검색 결과 DTO (친구 관계 정보 포함)
 * - LEFT JOIN Friendship으로 한번에 조회
 * - 탈퇴한 유저는 쿼리에서 이미 제외됨 (deletedAt IS NULL)
 */
public record UserSearchDto(
        Long userId,
        String nickname,
        String profileImageKey,
        DefaultProfileType defaultProfileType,
        Long friendshipId,
        FriendshipStatus friendshipStatus,
        Boolean isRequester  // 현재 유저가 요청자인지 여부 (요청 방향 판단용)
) {
    public RelationshipStatus getRelationshipStatus(Long currentUserId) {
        // 본인인 경우
        if (userId.equals(currentUserId)) {
            return RelationshipStatus.SELF;
        }

        if (friendshipId == null) {
            return RelationshipStatus.NONE;
        }
        if (friendshipStatus == FriendshipStatus.ACCEPTED) {
            return RelationshipStatus.FRIEND;
        }
        if (friendshipStatus == FriendshipStatus.PENDING) {
            return isRequester ? RelationshipStatus.REQUEST_SENT : RelationshipStatus.REQUEST_RECEIVED;
        }
        return RelationshipStatus.NONE;
    }
}