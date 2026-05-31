package com.devkor.ifive.nadab.domain.appversion.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "app_version_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_version_items_order", columnNames = {"app_version_id", "display_order"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppVersionItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "app_version_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_app_version_items_app_version")
    )
    private AppVersion appVersion;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
