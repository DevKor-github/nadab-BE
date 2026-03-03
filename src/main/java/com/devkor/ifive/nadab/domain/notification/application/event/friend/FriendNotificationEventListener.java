package com.devkor.ifive.nadab.domain.notification.application.event.friend;

import com.devkor.ifive.nadab.domain.friend.application.event.FriendRequestAcceptedEvent;
import com.devkor.ifive.nadab.domain.friend.application.event.FriendRequestReceivedEvent;
import com.devkor.ifive.nadab.domain.notification.application.NotificationCommandService;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationContent;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 친구 관련 알림 이벤트 리스너
 * - 친구 요청 수신 → 수신자에게 알림
 * - 친구 요청 수락 → 요청자에게 알림
 * - @Async로 비동기 처리 (친구 요청 처리 API 응답 속도 향상)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FriendNotificationEventListener {

    private final UserRepository userRepository;
    private final NotificationMessageFactory messageFactory;
    private final NotificationCommandService notificationCommandService;

    /**
     * 친구 요청 수신 알림
     * - 수신자에게 알림 전송
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleFriendRequestReceived(FriendRequestReceivedEvent event) {
        try {
            // 요청자 정보 조회
            User requester = userRepository.findById(event.getRequesterId())
                .orElse(null);

            // 요청자가 없거나 탈퇴한 경우 알림 생성 안 함
            if (requester == null || requester.getDeletedAt() != null) {
                log.info("Requester not found or deleted, skip notification: requesterId={}", event.getRequesterId());
                return;
            }

            // 메시지 생성
            Map<String, String> params = Map.of("senderName", requester.getNickname());
            NotificationContent content = messageFactory.createMessage(
                NotificationType.FRIEND_REQUEST_RECEIVED,
                params
            );

            // 알림 생성
            String idempotencyKey = String.format("FRIEND_REQUEST_%d", event.getFriendshipId());
            notificationCommandService.sendNotification(
                event.getReceiverId(),
                NotificationType.FRIEND_REQUEST_RECEIVED,
                content.title(),
                content.body(),
                content.inboxMessage(),
                event.getFriendshipId().toString(),
                idempotencyKey
            );

            log.debug("Friend request notification created: friendshipId={}, receiverId={}",
                event.getFriendshipId(), event.getReceiverId());

        } catch (Exception e) {
            log.error("Failed to handle friend request received event: friendshipId={}, error={}",
                event.getFriendshipId(), e.getMessage(), e);
        }
    }

    /**
     * 친구 요청 수락 알림
     * - 요청자에게 알림 전송
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        try {
            // 수락자 정보 조회
            User accepter = userRepository.findById(event.getAccepterId())
                .orElse(null);

            // 수락자가 없거나 탈퇴한 경우 알림 생성 안 함
            if (accepter == null || accepter.getDeletedAt() != null) {
                log.info("Accepter not found or deleted, skip notification: accepterId={}", event.getAccepterId());
                return;
            }

            // 메시지 생성
            Map<String, String> params = Map.of("senderName", accepter.getNickname());
            NotificationContent content = messageFactory.createMessage(
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                params
            );

            // 알림 생성
            String idempotencyKey = String.format("FRIEND_ACCEPTED_%d", event.getFriendshipId());
            notificationCommandService.sendNotification(
                event.getRequesterId(),
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                content.title(),
                content.body(),
                content.inboxMessage(),
                event.getFriendshipId().toString(),
                idempotencyKey
            );

            log.debug("Friend accepted notification created: friendshipId={}, requesterId={}",
                event.getFriendshipId(), event.getRequesterId());

        } catch (Exception e) {
            log.error("Failed to handle friend request accepted event: friendshipId={}, error={}",
                event.getFriendshipId(), e.getMessage(), e);
        }
    }
}