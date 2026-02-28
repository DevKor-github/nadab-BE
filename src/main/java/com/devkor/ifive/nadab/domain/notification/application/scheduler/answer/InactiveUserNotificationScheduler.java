package com.devkor.ifive.nadab.domain.notification.application.scheduler.answer;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.UserWithLastAnswerDate;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.notification.core.dto.NotificationMessageDto;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.domain.notification.core.entity.UserDevice;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationSettingRepository;
import com.devkor.ifive.nadab.domain.notification.core.repository.UserDeviceRepository;
import com.devkor.ifive.nadab.domain.notification.infra.FcmClient;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationContent;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 미답변 사용자 알림 스케줄러
 * - 매일 밤 22시 실행
 * - 2일 이상 미답변 사용자에게 알림
 * - 7일 미만/이상에 따라 다른 랜덤 메시지 전송
 * - 푸시만 발송 (알림함 저장 안 함)
 * - 중복 발송 방지 (같은 날 같은 사용자에게 한 번만)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InactiveUserNotificationScheduler {

    private final AnswerEntryRepository answerEntryRepository;
    private final NotificationMessageFactory messageFactory;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmClient fcmClient;

    private static final long MIN_INACTIVE_DAYS = 2;

    // 중복 발송 방지용 (오늘 발송한 사용자 ID 저장)
    private final Set<String> sentToday = ConcurrentHashMap.newKeySet();

    /**
     * 미답변 사용자 알림
     * - cron: 매일 밤 22시 (0 0 22 * * *)
     */
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void notifyInactiveUsers() {
        log.info("Starting inactive user notification scheduler");

        try {
            // 날짜 변경 시 중복 방지 Set 초기화
            LocalDate today = LocalDate.now();
            sentToday.clear();

            // 2일 이전 날짜 계산
            LocalDate cutoffDate = today.minusDays(MIN_INACTIVE_DAYS);

            // 마지막 답변일이 2일 이전인 사용자 조회
            List<UserWithLastAnswerDate> results = answerEntryRepository.findUsersWithLastAnswerBefore(cutoffDate);

            if (results.isEmpty()) {
                log.info("No inactive users found");
                return;
            }

            // 모든 사용자 데이터 미리 조회
            List<Long> userIds = results.stream()
                .map(r -> r.user().getId())
                .toList();

            // 알림 설정 한 번에 조회
            NotificationGroup group = NotificationType.INACTIVE_USER_REMINDER.getGroup();
            Map<Long, NotificationSetting> settingsMap = notificationSettingRepository
                .findByUserIdIn(userIds)
                .stream()
                .filter(s -> s.getGroup() == group)
                .collect(Collectors.toMap(s -> s.getUser().getId(), s -> s));

            // FCM 토큰 한 번에 조회
            Map<Long, List<UserDevice>> devicesMap = userDeviceRepository
                .findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.groupingBy(d -> d.getUser().getId()));

            int notificationCount = 0;
            int skippedCount = 0;

            for (UserWithLastAnswerDate result : results) {
                // 중복 발송 체크
                String sentKey = result.user().getId() + "_" + today;
                if (sentToday.contains(sentKey)) {
                    log.debug("Already sent today, skip: userId={}", result.user().getId());
                    skippedCount++;
                    continue;
                }

                // 미답변 일수 계산
                long daysInactive = ChronoUnit.DAYS.between(result.lastAnswerDate(), today);

                // 알림 발송
                NotificationSetting setting = settingsMap.get(result.user().getId());
                List<UserDevice> devices = devicesMap.getOrDefault(result.user().getId(), List.of());

                boolean sent = sendInactiveUserNotification(result.user(), daysInactive, setting, devices);
                if (sent) {
                    sentToday.add(sentKey);
                    notificationCount++;
                }
            }

            log.info("Inactive user notification scheduler completed: {} users notified, {} skipped",
                notificationCount, skippedCount);

        } catch (Exception e) {
            log.error("Failed to execute inactive user notification scheduler", e);
        }
    }

    /**
     * 미답변 사용자 알림 발송 (푸시만, 알림함 저장 안 함)
     */
    private boolean sendInactiveUserNotification(
        User user,
        long daysInactive,
        NotificationSetting setting,
        List<UserDevice> devices
    ) {
        try {
            // 1. 알림 설정 체크
            if (setting != null && !setting.isEnabled()) {
                log.info("User disabled notification group, skip: userId={}, group={}",
                    user.getId(), setting.getGroup());
                return false;
            }

            // 2. 메시지 생성 (7일 미만/이상에 따라 랜덤)
            Map<String, String> params = Map.of(
                "nickname", user.getNickname(),
                "daysInactive", String.valueOf(daysInactive)
            );

            NotificationContent content = messageFactory.createMessage(
                NotificationType.INACTIVE_USER_REMINDER,
                params
            );

            // 3. FCM 토큰 검증
            if (devices.isEmpty()) {
                log.info("No FCM tokens found, skip: userId={}", user.getId());
                return false;
            }

            List<String> tokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();

            if (tokens.isEmpty()) {
                log.info("No valid FCM tokens, skip: userId={}", user.getId());
                return false;
            }

            // 4. FCM 푸시 발송
            NotificationMessageDto messageDto = NotificationMessageDto.builder()
                .type(NotificationType.INACTIVE_USER_REMINDER)
                .title(content.title())
                .body(content.body())
                .inboxMessage(content.inboxMessage())
                .targetId(null)
                .unreadCount(0)  // 알림함에 저장 안 하므로 0
                .build();

            fcmClient.sendMulticast(tokens, messageDto);

            log.info("Inactive user push notification sent: userId={}, daysInactive={}, deviceCount={}",
                user.getId(), daysInactive, tokens.size());

            return true;

        } catch (Exception e) {
            log.error("Failed to send inactive user notification: userId={}, daysInactive={}",
                user.getId(), daysInactive, e);
            return false;
        }
    }
}