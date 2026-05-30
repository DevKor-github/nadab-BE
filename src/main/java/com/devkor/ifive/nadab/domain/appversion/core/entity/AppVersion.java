package com.devkor.ifive.nadab.domain.appversion.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;

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

    @Type(JsonType.class)
    @Column(name = "items", columnDefinition = "jsonb", nullable = false)
    private List<AppVersionItem> items;
}
