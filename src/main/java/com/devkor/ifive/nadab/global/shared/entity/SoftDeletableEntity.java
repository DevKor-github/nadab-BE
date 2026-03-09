package com.devkor.ifive.nadab.global.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    public void softDelete() {
        if (deletedAt != null) return;

        OffsetDateTime now = OffsetDateTime.now();
        this.updatedAt = now;
        this.deletedAt = now;
    }

    public void undoSoftDelete() {
        this.deletedAt = null;
    }
}