package com.devkor.ifive.nadab.domain.moderation.application;

import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.moderation.core.entity.UserBlock;
import com.devkor.ifive.nadab.domain.moderation.core.repository.UserBlockRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBlockCommandService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final FriendshipRepository friendshipRepository;

    public Long blockUser(Long blockerId, String blockedNickname) {
        User blocked = userRepository.findByNicknameAndDeletedAtIsNull(blockedNickname)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (blockerId.equals(blocked.getId())) {
            throw new BadRequestException(ErrorCode.MODERATION_CANNOT_BLOCK_SELF);
        }

        if (userBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blocked.getId())) {
            throw new BadRequestException(ErrorCode.MODERATION_ALREADY_BLOCKED);
        }

        User blocker = userRepository.getReferenceById(blockerId);
        UserBlock saved = userBlockRepository.save(UserBlock.create(blocker, blocked));

        Long userId1 = Math.min(blockerId, blocked.getId());
        Long userId2 = Math.max(blockerId, blocked.getId());
        friendshipRepository.deleteByUserIds(userId1, userId2);

        return saved.getId();
    }

    public void unblockUser(Long blockerId, Long userBlockId) {
        UserBlock userBlock = userBlockRepository.findByIdAndBlocker_Id(userBlockId, blockerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MODERATION_BLOCK_NOT_FOUND));
        userBlockRepository.delete(userBlock);
    }
}
