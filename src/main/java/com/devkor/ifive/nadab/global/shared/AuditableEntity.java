package com.devkor.ifive.nadab.global.shared;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
public abstract class AuditableEntity extends CreatableEntity {

    @Column(name = "updated_at")
    protected OffsetDateTime updatedAt;

    // Timestamped의 onCreate를 오버라이드
    @Override
    protected void onCreate() {
        super.onCreate();
        this.updatedAt = this.createdAt;
        postOnCreate();
    }

    // 각 엔티티별로 커스텀 로직이 필요하면 오버라이드
    protected void postOnCreate() {}

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
