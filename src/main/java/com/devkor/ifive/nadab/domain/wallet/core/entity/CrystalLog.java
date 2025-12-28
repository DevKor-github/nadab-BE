package com.devkor.ifive.nadab.domain.wallet.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "crystal_logs"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrystalLog extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_crystal_logs_user")
    )
    private User user;

    @Column(name = "delta", nullable = false)
    private long delta;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 64)
    private CrystalLogReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private CrystalLogStatus status;

    @Column(name = "ref_type", length = 64)
    private String refType;

    @Column(name = "ref_id", length = 128)
    private Long refId;

    public static CrystalLog createPending(User user, long delta, long balanceAfter, CrystalLogReason reason, String refType, Long refId) {
        CrystalLog log = create(user, delta, balanceAfter, reason, refType, refId);
        log.status = CrystalLogStatus.PENDING;
        return log;
    }

    public static CrystalLog createCompleted(User user, long delta, long balanceAfter, CrystalLogReason reason, String refType, Long refId) {
        CrystalLog log = create(user, delta, balanceAfter, reason, refType, refId);
        log.status = CrystalLogStatus.CONFIRMED;
        return log;
    }

    private static CrystalLog create(User user, long delta, long balanceAfter, CrystalLogReason reason, String refType, Long refId) {
        CrystalLog log = new CrystalLog();
        log.user = user;
        log.delta = delta;
        log.balanceAfter = balanceAfter;
        log.reason = reason;
        log.refType = refType;
        log.refId = refId;
        return log;
    }
}
