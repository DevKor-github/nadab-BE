package com.devkor.ifive.nadab.domain.friend.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 친구 요청 수락 이벤트
 * - 친구 요청이 수락되면 발행
 * - 요청자에게 알림 전송을 위한 트리거
 */
@Getter
@RequiredArgsConstructor
public class FriendRequestAcceptedEvent {

    private final Long friendshipId;
    private final Long requesterId;
    private final Long accepterId;
}
