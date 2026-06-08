package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_withdrawal_reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawalReason extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private WithdrawalReasonType reason;

    @Column(name = "custom_reason", length = 200)
    private String customReason;

    @Column(name = "withdrawn_at", nullable = false)
    private OffsetDateTime withdrawnAt;

    public static UserWithdrawalReason create(
            User user,
            WithdrawalReasonType reason,
            String customReason,
            OffsetDateTime withdrawnAt
    ) {
        UserWithdrawalReason userWithdrawalReason = new UserWithdrawalReason();
        userWithdrawalReason.user = user;
        userWithdrawalReason.reason = reason;
        userWithdrawalReason.customReason = customReason;
        userWithdrawalReason.withdrawnAt = withdrawnAt;
        return userWithdrawalReason;
    }
}
