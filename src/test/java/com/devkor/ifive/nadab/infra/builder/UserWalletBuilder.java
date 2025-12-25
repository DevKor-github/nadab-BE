package com.devkor.ifive.nadab.infra.builder;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class UserWalletBuilder {
    private final TestEntityManager em;
    private User user;
    private long crystalBalance;
    private long version;

    public UserWalletBuilder(TestEntityManager em) {
        this.em = em;
        this.crystalBalance = 0L;
        this.version = 0L;
    }

    public UserWalletBuilder user(User user) {
        this.user = user;
        return this;
    }

    public UserWallet build() {
        UserWallet wallet = UserWallet.create(user);
        em.persist(wallet);
        return wallet;
    }
}
