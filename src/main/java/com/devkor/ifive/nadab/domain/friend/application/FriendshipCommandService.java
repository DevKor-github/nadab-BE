package com.devkor.ifive.nadab.domain.friend.application;

import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipCommandService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public Long sendFriendRequest(Long requesterId, String receiverNickname) {
        // 1. 수신자 조회
        User receiver = userRepository.findByNicknameAndDeletedAtIsNull(receiverNickname)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Long receiverId = receiver.getId();

        // 2. 본인에게 요청 불가
        if (requesterId.equals(receiverId)) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_CANNOT_SEND_TO_SELF);
        }

        // 3. 발신자 친구 수 확인 (20명 초과 시 요청 불가)
        int requesterFriendCount = friendshipRepository.countByUserIdAndStatus(
                requesterId, FriendshipStatus.ACCEPTED
        );
        if (requesterFriendCount >= 20) {
            throw new BadRequestException(ErrorCode.FRIEND_LIMIT_EXCEEDED);
        }

        // 4. 수신자 친구 수 확인 (20명 초과 시 요청 불가)
        int receiverFriendCount = friendshipRepository.countByUserIdAndStatus(
                receiverId, FriendshipStatus.ACCEPTED
        );
        if (receiverFriendCount >= 20) {
            throw new BadRequestException(ErrorCode.FRIEND_RECEIVER_LIMIT_EXCEEDED);
        }

        // 5. 기존 관계 확인
        Long userId1 = Math.min(requesterId, receiverId);
        Long userId2 = Math.max(requesterId, receiverId);

        if (friendshipRepository.existsByUserIds(userId1, userId2)) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_ALREADY_EXISTS);
        }

        // 6. 친구 요청 생성
        User requester = userRepository.getReferenceById(requesterId);
        Friendship friendship = Friendship.createPending(requester, receiver);

        Friendship saved = friendshipRepository.save(friendship);
        return saved.getId();
    }

    public void cancelFriendRequest(Long userId, Long friendshipId) {
        // 1. Friendship 조회
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        // 2. 권한 확인 (본인이 보낸 요청만 취소 가능)
        if (!friendship.isRequester(userId)) {
            throw new ForbiddenException(ErrorCode.FRIENDSHIP_ACCESS_FORBIDDEN);
        }

        // 3. 상태 확인
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        // 4. 삭제
        friendshipRepository.delete(friendship);
    }

    public void acceptFriendRequest(Long userId, Long friendshipId) {
        // 1. Friendship 조회
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        // 2. 권한 확인 (receiver만 수락 가능)
        if (!friendship.isReceiver(userId)) {
            throw new ForbiddenException(ErrorCode.FRIENDSHIP_ACCESS_FORBIDDEN);
        }

        // 3. 상태 확인
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        // 4. 친구 수 확인 (양쪽 모두)
        Long requesterId = friendship.getOtherUserId(userId);

        int requesterFriendCount = friendshipRepository.countByUserIdAndStatus(
                requesterId, FriendshipStatus.ACCEPTED
        );
        int receiverFriendCount = friendshipRepository.countByUserIdAndStatus(
                userId, FriendshipStatus.ACCEPTED
        );

        if (requesterFriendCount >= 20) {
            throw new BadRequestException(ErrorCode.FRIEND_RECEIVER_LIMIT_EXCEEDED);
        }
        if (receiverFriendCount >= 20) {
            throw new BadRequestException(ErrorCode.FRIEND_LIMIT_EXCEEDED);
        }

        // 5. 상태 변경
        friendship.accept();
    }

    public void rejectFriendRequest(Long userId, Long friendshipId) {
        // 1. Friendship 조회
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        // 2. 권한 확인 (receiver만 거절 가능)
        if (!friendship.isReceiver(userId)) {
            throw new ForbiddenException(ErrorCode.FRIENDSHIP_ACCESS_FORBIDDEN);
        }

        // 3. 상태 확인
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        // 4. 삭제
        friendshipRepository.delete(friendship);
    }

    public void deleteFriend(Long userId, Long friendshipId) {
        // 1. Friendship 조회 및 권한 확인
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        if (!friendship.involves(userId)) {
            throw new ForbiddenException(ErrorCode.FRIENDSHIP_ACCESS_FORBIDDEN);
        }

        // 2. 상태 확인 (ACCEPTED만 삭제 가능)
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        // 3. Friendship 삭제
        friendshipRepository.delete(friendship);
    }
}
