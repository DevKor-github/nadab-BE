package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "withdrawal_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_withdrawal_events_user_withdrawn_at",
                        columnNames = {"user_id", "withdrawn_at"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalEvent extends CreatableEntity {

    private static final int RETENTION_YEARS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "withdrawn_at", nullable = false)
    private OffsetDateTime withdrawnAt;

    @Column(name = "anonymized_at")
    private OffsetDateTime anonymizedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    public static WithdrawalEvent create(User user, OffsetDateTime withdrawnAt) {
        WithdrawalEvent event = new WithdrawalEvent();
        event.user = user;
        event.withdrawnAt = withdrawnAt;
        event.expiresAt = withdrawnAt.plusYears(RETENTION_YEARS);
        return event;
    }
}
