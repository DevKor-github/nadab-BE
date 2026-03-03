package com.devkor.ifive.nadab.domain.notification.infra;

import com.devkor.ifive.nadab.domain.notification.core.dto.NotificationMessageDto;
import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus;
import com.devkor.ifive.nadab.domain.notification.core.entity.UserDevice;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationSettingRepository;
import com.devkor.ifive.nadab.domain.notification.core.repository.UserDeviceRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FCM 알림 발송기 (Infra 레이어)
 * - sendInternal(): 개별 알림 발송 (EventListener에서 호출)
 * - sendBatch(): 배치 알림 발송 (Scheduler에서 호출)
 * - sendSilentBadgeUpdate(): 비동기 메서드 (실시간 뱃지 동기화)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FcmNotificationSender {

    private final FcmClient fcmClient;
    private final UserDeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    /**
     * 개별 알림 발송
     * - EventListener에서 호출 (즉시 발송)
     * - NotificationSetting 확인
     * - Multicast 발송
     * - Invalid Token 자동 삭제
     */
    public boolean sendInternal(Notification notification) {
        User user = notification.getUser();

        // 탈퇴한 사용자는 발송 안 함
        if (user.getDeletedAt() != null) {
            log.info("User is deleted, skip notification: userId={}, notificationId={}",
                    user.getId(), notification.getId());
            return false;
        }

        // NotificationSetting 확인 (사용자가 해당 그룹 알림을 껐는지)
        NotificationGroup group = notification.getType().getGroup();
        boolean isEnabled = notificationSettingRepository
                .findByUserAndGroup(user, group)
                .map(NotificationSetting::isEnabled)
                .orElse(true);  // 설정이 없으면 기본적으로 활성화

        if (!isEnabled) {
            log.info("Notification disabled by user settings: userId={}, group={}, notificationId={}",
                    user.getId(), group, notification.getId());
            return false;
        }

        // 디바이스 조회
        List<UserDevice> devices = deviceRepository.findByUser(user);

        if (devices.isEmpty()) {
            log.warn("No devices found for user: userId={}", user.getId());
            return false;
        }

        // 읽지 않은 알림 개수 (뱃지용)
        int unreadCount = (int) notificationRepository.countUnreadByUser(user);

        NotificationMessageDto message = NotificationMessageDto.from(notification, unreadCount);

        // 모든 디바이스의 토큰 수집
        List<String> fcmTokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .toList();

        // Multicast 발송 (Invalid Token 자동 정리 포함)
        List<String> validTokens = fcmClient.sendMulticast(fcmTokens, message);

        // 성공 여부 판단: 최소 1개 이상 발송 성공
        boolean success = !validTokens.isEmpty();

        if (!success) {
            log.warn("All devices have invalid tokens: userId={}, totalDevices={}",
                    user.getId(), devices.size());
        }

        return success;
    }

    /**
     * 배치 알림 발송
     * - Scheduler에서 호출
     * - 여러 알림을 효율적으로 처리
     */
    public Map<Long, Boolean> sendBatch(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 사용자 ID 목록 추출
        List<Long> userIds = notifications.stream()
            .map(n -> n.getUser().getId())
            .distinct()
            .toList();

        // 2. 배치 조회
        // 2-1. UserDevice 배치 조회
        Map<Long, List<UserDevice>> deviceMap = deviceRepository.findByUserIdIn(userIds)
            .stream()
            .collect(Collectors.groupingBy(d -> d.getUser().getId()));

        // 2-2. 읽지 않은 알림 개수 배치 조회
        Map<Long, Long> unreadCountMap = notificationRepository.countUnreadByUserIds(userIds)
            .stream()
            .collect(Collectors.toMap(
                NotificationRepository.UnreadCountProjection::getUserId,
                NotificationRepository.UnreadCountProjection::getUnreadCount
            ));

        // 2-3. NotificationSetting 배치 조회
        Map<Long, Map<NotificationGroup, Boolean>> settingMap = loadNotificationSettings(userIds);

        // 3. 각 알림 발송 (메모리에서 조합)
        Map<Long, Boolean> results = new HashMap<>();
        for (Notification notification : notifications) {
            Long userId = notification.getUser().getId();

            // NotificationSetting 확인
            NotificationGroup group = notification.getType().getGroup();
            boolean isEnabled = settingMap.getOrDefault(userId, Collections.emptyMap())
                .getOrDefault(group, true);

            if (!isEnabled) {
                log.info("Notification disabled by user settings: userId={}, group={}, notificationId={}",
                    userId, group, notification.getId());
                results.put(notification.getId(), false);
                continue;
            }

            // 디바이스 조회
            List<UserDevice> devices = deviceMap.getOrDefault(userId, Collections.emptyList());
            if (devices.isEmpty()) {
                log.warn("No devices found for user: userId={}", userId);
                results.put(notification.getId(), false);
                continue;
            }

            // 읽지 않은 알림 개수
            int unreadCount = unreadCountMap.getOrDefault(userId, 0L).intValue();

            // FCM 발송
            NotificationMessageDto message = NotificationMessageDto.from(notification, unreadCount);
            List<String> fcmTokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .toList();

            List<String> validTokens = fcmClient.sendMulticast(fcmTokens, message);
            boolean success = !validTokens.isEmpty();

            if (!success) {
                log.warn("All devices have invalid tokens: userId={}, totalDevices={}",
                    userId, devices.size());
            }

            results.put(notification.getId(), success);
        }

        log.debug("Batch send completed: total={}, success={}, failed={}",
            notifications.size(),
            results.values().stream().filter(b -> b).count(),
            results.values().stream().filter(b -> !b).count());

        return results;
    }

    /**
     * NotificationSetting 배치 조회
     */
    private Map<Long, Map<NotificationGroup, Boolean>> loadNotificationSettings(List<Long> userIds) {
        // 1. 모든 사용자의 NotificationSetting 한 번에 조회
        List<NotificationSetting> allSettings = notificationSettingRepository.findByUserIdIn(userIds);

        // 2. userId → (group → enabled) Map 구조로 변환
        Map<Long, Map<NotificationGroup, Boolean>> settingMap = new HashMap<>();

        // 2-1. DB에서 조회한 설정을 Map에 저장
        for (NotificationSetting setting : allSettings) {
            Long userId = setting.getUser().getId();
            NotificationGroup group = setting.getGroup();
            boolean enabled = setting.isEnabled();

            settingMap.computeIfAbsent(userId, k -> new HashMap<>())
                .put(group, enabled);
        }

        // 2-2. 설정이 없는 그룹은 기본값(true) 적용
        for (Long userId : userIds) {
            Map<NotificationGroup, Boolean> userSettings =
                settingMap.computeIfAbsent(userId, k -> new HashMap<>());

            for (NotificationGroup group : NotificationGroup.values()) {
                userSettings.putIfAbsent(group, true);  // 기본값: 활성화
            }
        }

        return settingMap;
    }

    /**
     * Silent Badge Update (알림 없이 뱃지만 업데이트)
     * - 알림 읽음/삭제 후 다른 기기 뱃지 동기화
     * - 비동기로 즉시 실행 (실시간 동기화)
     */
    @Async("fcmTaskExecutor")
    public void sendSilentBadgeUpdate(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for badge update: userId={}", userId);
            return;
        }

        // 읽지 않은 알림 개수
        int unreadCount = (int) notificationRepository.countUnreadByUser(user);

        // 모든 디바이스의 FCM 토큰 수집
        List<UserDevice> devices = deviceRepository.findByUser(user);
        List<String> fcmTokens = devices.stream()
            .map(UserDevice::getFcmToken)
            .toList();

        if (fcmTokens.isEmpty()) {
            log.debug("No devices to sync badge: userId={}", userId);
            return;
        }

        // Silent Badge Update 발송
        fcmClient.sendSilentBadgeUpdate(fcmTokens, unreadCount);
        log.debug("Silent badge update sent: userId={}, unreadCount={}, devices={}",
            userId, unreadCount, fcmTokens.size());
    }
}