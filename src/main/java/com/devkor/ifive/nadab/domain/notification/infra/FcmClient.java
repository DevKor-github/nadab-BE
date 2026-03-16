package com.devkor.ifive.nadab.domain.notification.infra;

import com.devkor.ifive.nadab.domain.notification.core.dto.NotificationMessageDto;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.FcmException;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebase Cloud Messaging 발송 클라이언트
 * - FCM 발송 및 Invalid Token 자동 정리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FcmClient {

    private final FirebaseMessaging firebaseMessaging;
    private final InvalidTokenCleaner invalidTokenCleaner;

    /**
     * 여러 디바이스에 일괄 발송 (Multicast)
     * - Invalid token 자동 정리 포함
     */
    public List<String> sendMulticast(List<String> fcmTokens, NotificationMessageDto message) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return List.of();
        }

        try {
            // 뱃지 카운트 (iOS: 999, Android: 자동)
            int badge = Math.max(0, Math.min(message.getUnreadCount(), 999));

            // iOS 설정 (APNs)
            ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setBadge(badge)
                    .setSound("default")  // 시스템 기본 사운드
                    .build())
                .build();

            // Android 설정
            AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("default_push")
                    .setSound("default")  // 시스템 기본 사운드
                    .setDefaultSound(true)
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .build())
                .build();

            MulticastMessage fcmMessage = MulticastMessage.builder()
                .setNotification(Notification.builder()
                    .setTitle(message.getTitle())
                    .setBody(message.getBody())
                    .build())
                .putData("type", message.getType().name())
                .putData("targetId", message.getTargetId() != null ? message.getTargetId() : "")
                .putData("inboxMessage", message.getInboxMessage())
                .putData("unreadCount", String.valueOf(message.getUnreadCount()))
                .setApnsConfig(apnsConfig)  // iOS 설정
                .setAndroidConfig(androidConfig)  // Android 설정
                .addAllTokens(fcmTokens)
                .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(fcmMessage);

            log.debug("FCM multicast sent: total={}, success={}, failure={}",
                response.getResponses().size(), response.getSuccessCount(), response.getFailureCount());

            // Invalid token 자동 정리
            invalidTokenCleaner.cleanupInvalidTokens(fcmTokens, response);

            // 유효한 토큰만 반환
            return getValidTokens(fcmTokens, response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast send failed: error={}", e.getMessage());
            throw new FcmException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    /**
     * Silent Badge Update (알림 없이 뱃지만 업데이트)
     * - 다른 기기에서 알림을 읽었을 때 뱃지 동기화
     * - 알림창에 표시되지 않음 (조용히 뱃지만 변경)
     */
    public void sendSilentBadgeUpdate(List<String> fcmTokens, int unreadCount) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        try {
            int badge = Math.max(0, Math.min(unreadCount, 999));

            // iOS
            ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setBadge(badge)
                    .setContentAvailable(true)  // Silent push
                    .build())
                .build();

            // Android
            AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.NORMAL)
                .build();

            MulticastMessage message = MulticastMessage.builder()
                .putData("type", "UPDATE_BADGE")
                .putData("silent", "true")
                .putData("unreadCount", String.valueOf(unreadCount))
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig)
                .addAllTokens(fcmTokens)
                .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

            log.debug("Silent badge update sent: total={}, success={}, failure={}, badge={}",
                response.getResponses().size(), response.getSuccessCount(),
                response.getFailureCount(), badge);

            // Invalid token 자동 정리
            invalidTokenCleaner.cleanupInvalidTokens(fcmTokens, response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM silent badge update failed: error={}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected FCM silent badge update error: {}", e.getMessage(), e);
        }
    }

    /**
     * 유효한 토큰만 추출 (Invalid 토큰 제외)
     */
    private List<String> getValidTokens(List<String> fcmTokens, BatchResponse response) {
        List<String> validTokens = new ArrayList<>();
        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                validTokens.add(fcmTokens.get(i));
            }
        }

        return validTokens;
    }
}