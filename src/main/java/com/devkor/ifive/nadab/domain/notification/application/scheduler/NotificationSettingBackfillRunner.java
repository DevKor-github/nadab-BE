package com.devkor.ifive.nadab.domain.notification.application.scheduler;

import com.devkor.ifive.nadab.domain.notification.application.NotificationSettingService;
import com.devkor.ifive.nadab.domain.notification.core.repository.UserDeviceRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 알림 설정 백필 러너
 * - FCM 디바이스가 등록된 유저 중 알림 설정이 없는 유저에게 기본값으로 생성
 * - 백필 완료 후 삭제 예정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingBackfillRunner implements ApplicationRunner {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingService notificationSettingService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<User> usersWithDevices = userDeviceRepository.findDistinctActiveUsers();

            int count = 0;
            for (User user : usersWithDevices) {
                try {
                    notificationSettingService.ensureSettingsExist(user);
                    count++;
                } catch (Exception e) {
                    log.error("Failed to initialize notification settings: userId={}", user.getId(), e);
                }
            }

            log.info("NotificationSetting backfill completed: {}/{} users processed", count, usersWithDevices.size());
        } catch (Exception e) {
            log.error("NotificationSetting backfill runner failed", e);
        }
    }
}