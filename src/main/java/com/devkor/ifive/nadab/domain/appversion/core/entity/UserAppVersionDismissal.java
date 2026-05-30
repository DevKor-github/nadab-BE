package com.devkor.ifive.nadab.domain.appversion.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_app_version_dismissals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_uavd_user_app_version", columnNames = {"user_id", "app_version_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAppVersionDismissal extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_uavd_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "app_version_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_uavd_app_version")
    )
    private AppVersion appVersion;

    public static UserAppVersionDismissal create(User user, AppVersion appVersion) {
        UserAppVersionDismissal dismissal = new UserAppVersionDismissal();
        dismissal.user = user;
        dismissal.appVersion = appVersion;
        return dismissal;
    }
}
