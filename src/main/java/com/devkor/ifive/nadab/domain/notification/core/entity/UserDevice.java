package com.devkor.ifive.nadab.domain.notification.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_devices",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_fcm_token", columnNames = {"fcm_token"}),
        @UniqueConstraint(name = "uk_user_device", columnNames = {"user_id", "device_id", "platform"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDevice extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatform platform;

    public static UserDevice create(User user, String fcmToken, String deviceId, DevicePlatform platform) {
        UserDevice device = new UserDevice();
        device.user = user;
        device.fcmToken = fcmToken;
        device.deviceId = deviceId;
        device.platform = platform;
        return device;
    }

    public void updateToken(String newToken) {
        this.fcmToken = newToken;
    }
}