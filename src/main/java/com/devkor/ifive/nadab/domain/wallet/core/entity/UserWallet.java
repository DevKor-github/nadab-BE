package com.devkor.ifive.nadab.domain.wallet.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_wallets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_wallets_user_id", columnNames = {"user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWallet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "crystal_balance", nullable = false)
    private long crystalBalance;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public static UserWallet create(User user) {
        UserWallet wallet = new UserWallet();
        wallet.user = user;
        wallet.crystalBalance = 0L;
        return wallet;
    }
}
