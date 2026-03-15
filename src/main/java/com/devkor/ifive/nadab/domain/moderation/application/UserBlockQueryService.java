package com.devkor.ifive.nadab.domain.moderation.application;

import com.devkor.ifive.nadab.domain.moderation.api.dto.response.BlockedUserListResponse;
import com.devkor.ifive.nadab.domain.moderation.api.dto.response.BlockedUserResponse;
import com.devkor.ifive.nadab.domain.moderation.core.entity.UserBlock;
import com.devkor.ifive.nadab.domain.moderation.core.repository.UserBlockRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockQueryService {

    private final UserBlockRepository userBlockRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public BlockedUserListResponse getBlockedUsers(Long blockerId) {
        List<UserBlock> userBlocks = userBlockRepository.findByBlockerIdWithBlockedUser(blockerId);

        List<BlockedUserResponse> blockedUsers = userBlocks.stream()
                .map(userBlock -> new BlockedUserResponse(
                        userBlock.getId(),
                        userBlock.getBlocked().getNickname(),
                        profileImageUrlBuilder.buildUserProfileUrl(userBlock.getBlocked())
                ))
                .toList();

        return new BlockedUserListResponse(blockedUsers.size(), blockedUsers);
    }
}
