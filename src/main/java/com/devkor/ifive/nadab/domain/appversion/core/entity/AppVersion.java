package com.devkor.ifive.nadab.domain.appversion.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "app_versions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_versions_platform_version", columnNames = {"platform", "version"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppVersion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private AppPlatform platform;

    @Column(name = "version", nullable = false, length = 30)
    private String version;

    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest;

    @Column(name = "summary", nullable = false, length = 120)
    private String summary;

    private AppVersion(AppPlatform platform, String version, boolean isLatest, String summary) {
        this.platform = platform;
        this.version = version;
        this.isLatest = isLatest;
        this.summary = summary;
    }

    public static AppVersion create(AppPlatform platform, String version, String summary) {
        return new AppVersion(platform, version, true, summary);
    }

    public void markAsLatest() {
        this.isLatest = true;
    }

    public void markAsNotLatest() {
        this.isLatest = false;
    }
}
