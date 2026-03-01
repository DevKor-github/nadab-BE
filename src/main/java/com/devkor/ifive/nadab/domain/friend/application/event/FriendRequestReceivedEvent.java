package com.devkor.ifive.nadab.domain.friend.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 친구 요청 수신 이벤트
 * - 친구 요청이 생성되면 발행
 * - 수신자에게 알림 전송을 위한 트리거
 */
@Getter
@RequiredArgsConstructor
public class FriendRequestReceivedEvent {

    private final Long friendshipId;
    private final Long receiverId;
    private final Long requesterId;
}
