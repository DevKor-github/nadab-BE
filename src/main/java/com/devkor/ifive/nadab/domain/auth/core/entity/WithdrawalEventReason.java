package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "withdrawal_event_reasons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_withdrawal_event_reasons_event_reason",
                        columnNames = {"event_id", "reason"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalEventReason extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private WithdrawalEvent event;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private WithdrawalReasonType reason;

    @Column(name = "custom_reason", length = 200)
    private String customReason;

    public static WithdrawalEventReason create(
            WithdrawalEvent event,
            WithdrawalReasonType reason,
            String customReason
    ) {
        WithdrawalEventReason eventReason = new WithdrawalEventReason();
        eventReason.event = event;
        eventReason.reason = reason;
        eventReason.customReason = customReason;
        return eventReason;
    }
}
