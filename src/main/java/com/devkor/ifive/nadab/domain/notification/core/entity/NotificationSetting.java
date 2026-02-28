package com.devkor.ifive.nadab.domain.notification.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 알림 설정 (사용자별, 그룹별)
 * - 사용자는 알림 그룹별로 on/off 가능
 * - ACTIVITY_REMINDER 그룹은 추가로 daily_write_time 설정 가능
 */
@Entity
@Table(
    name = "notification_settings",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_group",
        columnNames = {"user_id", "notification_group"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_group", nullable = false, length = 50)
    private NotificationGroup group;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * 작성 알림 시간 (ACTIVITY_REMINDER 그룹인 경우만 사용)
     * - DAILY_WRITE_REMINDER: 이 시간에 알림 발송 (사용자 설정)
     */
    @Column(name = "daily_write_time")
    private LocalTime dailyWriteTime;

    public static NotificationSetting create(User user, NotificationGroup group) {
        NotificationSetting setting = new NotificationSetting();
        setting.user = user;
        setting.group = group;
        setting.enabled = true;

        // ACTIVITY_REMINDER 그룹이면 기본값 설정
        if (group == NotificationGroup.ACTIVITY_REMINDER) {
            setting.dailyWriteTime = LocalTime.of(20, 0);
        }

        return setting;
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void updateDailyWriteTime(LocalTime time) {
        this.dailyWriteTime = time;
    }
}