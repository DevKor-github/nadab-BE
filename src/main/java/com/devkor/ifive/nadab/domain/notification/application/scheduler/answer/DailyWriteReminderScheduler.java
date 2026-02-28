package com.devkor.ifive.nadab.domain.notification.application.scheduler.answer;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.notification.core.dto.NotificationMessageDto;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.domain.notification.core.entity.UserDevice;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 일일 작성 알림 스케줄러
 * - 매분마다 실행 (00:00 ~ 23:59)
 * - 사용자가 설정한 알림 시간(시:분)에 정확히 푸시 발송
 * - 이미 오늘 작성 완료/작성 중인 사용자는 제외
 * - 푸시만 발송 (알림함 저장 안 함)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyWriteReminderScheduler {

    private final NotificationSettingRepository notificationSettingRepository;
    private final AnswerEntryRepository answerEntryRepository;
    private final NotificationMessageFactory messageFactory;
    private final UserDeviceRepository userDeviceRepository;
    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;

    /**
     * 일일 작성 알림
     * - cron: 매분 0초에 실행 (0 * * * * *)
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void sendDailyWriteReminder() {
        // 현재 시간 (초, 나노초 제거하여 분 단위까지만 비교)
        LocalTime currentTime = LocalTime.now()
            .withSecond(0)
            .withNano(0);

        log.debug("Starting daily write reminder scheduler: {}", currentTime);

        try {
            LocalDate today = LocalDate.now();

            // 1. 현재 시:분에 알림 받을 사용자 조회
            List<User> targetUsers = notificationSettingRepository.findUsersForDailyWriteReminder(currentTime);

            if (targetUsers.isEmpty()) {
                log.trace("No users to notify at {}", currentTime);
                return;
            }

            log.info("Found {} users for daily write reminder at {}", targetUsers.size(), currentTime);

            // 2. 오늘 이미 답변한 사용자 제외
            List<Long> targetUserIds = targetUsers.stream()
                .map(User::getId)
                .toList();

            Set<Long> answeredUserIds = answerEntryRepository
                .findUserIdsWithAnswerOnDate(targetUserIds, today)
                .stream()
                .collect(Collectors.toSet());

            List<User> usersToNotify = targetUsers.stream()
                .filter(user -> !answeredUserIds.contains(user.getId()))
                .toList();

            if (usersToNotify.isEmpty()) {
                log.info("All users already answered today, skip notification");
                return;
            }

            log.info("Users to notify after filtering: {} (answered: {})",
                usersToNotify.size(), answeredUserIds.size());

            // 3. FCM 토큰 조회
            List<Long> userIdsToNotify = usersToNotify.stream()
                .map(User::getId)
                .toList();

            Map<Long, List<UserDevice>> devicesMap = userDeviceRepository
                .findByUserIdIn(userIdsToNotify)
                .stream()
                .collect(Collectors.groupingBy(d -> d.getUser().getId()));

            // 4. 알림 발송
            int notificationCount = 0;
            int skippedCount = 0;

            for (User user : usersToNotify) {
                List<UserDevice> devices = devicesMap.getOrDefault(user.getId(), List.of());

                boolean sent = sendDailyWriteNotification(user, devices);
                if (sent) {
                    notificationCount++;
                } else {
                    skippedCount++;
                }
            }

            log.info("Daily write reminder scheduler completed at {}: {} users notified, {} skipped",
                currentTime, notificationCount, skippedCount);

        } catch (Exception e) {
            log.error("Failed to execute daily write reminder scheduler at {}",
                currentTime, e);
        }
    }

    /**
     * 일일 작성 알림 발송 (푸시만, 알림함 저장 안 함)
     */
    private boolean sendDailyWriteNotification(User user, List<UserDevice> devices) {
        try {
            // 1. FCM 토큰 검증
            if (devices.isEmpty()) {
                log.debug("No FCM tokens found, skip: userId={}", user.getId());
                return false;
            }

            List<String> tokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();

            if (tokens.isEmpty()) {
                log.debug("No valid FCM tokens, skip: userId={}", user.getId());
                return false;
            }

            // 2. 메시지 생성
            NotificationContent content = messageFactory.createMessage(
                NotificationType.DAILY_WRITE_REMINDER,
                Map.of()
            );

            // 3. 실제 읽지 않은 알림 개수 조회 (뱃지 동기화)
            int unreadCount = (int) notificationRepository.countUnreadByUser(user);

            // 4. FCM 푸시 발송
            NotificationMessageDto messageDto = NotificationMessageDto.builder()
                .type(NotificationType.DAILY_WRITE_REMINDER)
                .title(content.title())
                .body(content.body())
                .inboxMessage(content.inboxMessage())
                .targetId(null)
                .unreadCount(unreadCount)  // 실제 알림함 개수로 뱃지 유지
                .build();

            fcmClient.sendMulticast(tokens, messageDto);

            log.debug("Daily write reminder push notification sent: userId={}, deviceCount={}",
                user.getId(), tokens.size());

            return true;

        } catch (Exception e) {
            log.error("Failed to send daily write reminder notification: userId={}", user.getId(), e);
            return false;
        }
    }
}