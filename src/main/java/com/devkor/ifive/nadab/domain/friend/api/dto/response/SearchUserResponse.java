package com.devkor.ifive.nadab.domain.friend.api.dto.response;

import com.devkor.ifive.nadab.domain.user.core.dto.UserSearchDto;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 검색 결과 항목")
public record SearchUserResponse(
        @Schema(description = "친구 관계 ID (친구 요청 취소/수락/거절/삭제 시 사용, NONE 상태일 땐 null)", example = "123")
        Long friendshipId,

        @Schema(description = "닉네임", example = "춤추는사막여우")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/abc123.png")
        String profileImageUrl,

        @Schema(description = "친구 관계 상태", example = "NONE")
        RelationshipStatus relationshipStatus
) {
    public static SearchUserResponse of(
            User user,
            RelationshipStatus status,
            ProfileImageUrlBuilder urlBuilder
    ) {
        return new SearchUserResponse(
                null,  // 기존 메서드에서는 friendshipId 없음
                user.getNickname(),
                urlBuilder.buildUserProfileUrl(user),
                status
        );
    }

    public static SearchUserResponse from(UserSearchDto dto, Long currentUserId, ProfileImageUrlBuilder urlBuilder) {
        // ProfileImageUrlBuilder 로직 재현
        String profileImageUrl = null;
        if (dto.profileImageKey() != null) {
            profileImageUrl = urlBuilder.buildUrl(dto.profileImageKey());
        } else if (dto.defaultProfileType() != null) {
            profileImageUrl = urlBuilder.buildDefaultUrl(dto.defaultProfileType());
        }

        return new SearchUserResponse(
                dto.friendshipId(),
                dto.nickname(),
                profileImageUrl,
                dto.getRelationshipStatus(currentUserId)
        );
    }
}