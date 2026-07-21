package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "ask_chat_wallets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ask_chat_wallets_user_id", columnNames = {"user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatWallet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_wallets_user")
    )
    private User user;

    @Column(name = "free_turn_balance", nullable = false)
    private int freeTurnBalance;

    @Column(name = "paid_turn_balance", nullable = false)
    private int paidTurnBalance;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public static AskChatWallet create(User user) {
        return create(user, 0, 0);
    }

    public static AskChatWallet create(User user, int initialFreeTurnBalance, int initialPaidTurnBalance) {
        validateNonNegative(initialFreeTurnBalance);
        validateNonNegative(initialPaidTurnBalance);

        AskChatWallet wallet = new AskChatWallet();
        wallet.user = user;
        wallet.freeTurnBalance = initialFreeTurnBalance;
        wallet.paidTurnBalance = initialPaidTurnBalance;
        return wallet;
    }

    public int getTotalTurnBalance() {
        return freeTurnBalance + paidTurnBalance;
    }

    private static void validateNonNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Ask Chat turn balance must not be negative.");
        }
    }
}
