package com.devkor.ifive.nadab.domain.notification.application;

import com.devkor.ifive.nadab.domain.notification.api.dto.request.UpdateNotificationSettingRequest;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationSettingRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 모든 알림 설정 조회
     * - 없는 그룹은 기본값으로 생성
     */
    public List<NotificationSetting> getSettings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        List<NotificationSetting> settings = notificationSettingRepository.findByUser(user);

        // 없는 그룹만 기본값으로 생성
        Set<NotificationGroup> existingGroups = settings.stream()
            .map(NotificationSetting::getGroup)
            .collect(Collectors.toSet());

        for (NotificationGroup group : NotificationGroup.values()) {
            if (!existingGroups.contains(group)) {
                NotificationSetting setting = NotificationSetting.create(user, group);
                try {
                    notificationSettingRepository.save(setting);
                    settings.add(setting);
                } catch (DataIntegrityViolationException e) {
                    // 동시성으로 인해 이미 생성된 경우 (Race Condition)
                    // 다시 조회해서 추가
                    NotificationSetting existingSetting = notificationSettingRepository
                        .findByUserAndGroup(user, group)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_SETTINGS_NOT_FOUND));
                    settings.add(existingSetting);
                }
            }
        }

        // enum 순서대로 정렬
        return settings.stream()
            .sorted(Comparator.comparing(s -> s.getGroup().ordinal()))
            .toList();
    }

    /**
     * 여러 그룹의 알림 설정 일괄 수정
     * - 1개 이상의 그룹 설정을 한 번에 수정
     */
    public void updateSettings(Long userId, List<UpdateNotificationSettingRequest> requests) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        for (UpdateNotificationSettingRequest request : requests) {
            updateSettingInternal(user, request.group(), request.enabled(), request.dailyWriteTime());
        }
    }

    private void updateSettingInternal(
        User user,
        NotificationGroup group,
        Boolean enabled,
        LocalTime dailyWriteTime
    ) {
        Optional<NotificationSetting> settingOpt = notificationSettingRepository.findByUserAndGroup(user, group);

        NotificationSetting setting;
        if (settingOpt.isPresent()) {
            setting = settingOpt.get();
        } else {
            // 새 설정 생성 및 즉시 저장
            setting = NotificationSetting.create(user, group);
            notificationSettingRepository.save(setting);
        }

        // enabled 업데이트 (값이 다를 때만)
        if (enabled != setting.isEnabled()) {
            setting.updateEnabled(enabled);
        }

        // dailyWriteTime 업데이트 (ACTIVITY_REMINDER 그룹이고 값이 다를 때만)
        if (dailyWriteTime != null
            && group == NotificationGroup.ACTIVITY_REMINDER
            && !dailyWriteTime.equals(setting.getDailyWriteTime())) {
            setting.updateDailyWriteTime(dailyWriteTime);
        }
    }
}