package com.devkor.ifive.nadab.domain.wallet;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.builder.UserWalletBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserWalletRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    UserWalletRepository userWalletRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void charge_crystal() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        int updated = userWalletRepository.charge(user.getId(), 1000L);

        em.flush(); // 여기서 SQL 실행 강제
        em.clear(); // 1차 캐시 비우고 DB에서 다시 읽게 함

        //then
        UserWallet reloaded = userWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(reloaded.getCrystalBalance()).isEqualTo(1000L);
    }

    @Test
    void refund_crystal() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        int updated = userWalletRepository.refund(user.getId(), 300L);

        em.flush(); // 여기서 SQL 실행 강제
        em.clear(); // 1차 캐시 비우고 DB에서 다시 읽게 함

        //then
        UserWallet reloaded = userWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(reloaded.getCrystalBalance()).isEqualTo(300L);
    }

    @Test
    void consume_crystal() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        userWalletRepository.refund(user.getId(), 2000L);
        int updated = userWalletRepository.tryConsume(user.getId(), 1500L);

        em.flush(); // 여기서 SQL 실행 강제
        em.clear(); // 1차 캐시 비우고 DB에서 다시 읽게 함

        //then
        UserWallet reloaded = userWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(reloaded.getCrystalBalance()).isEqualTo(500L);
    }

    @Test
    void consume_crystal_insufficient_balance() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        int updated = userWalletRepository.tryConsume(user.getId(), 1500L);

        em.flush(); // 여기서 SQL 실행 강제
        em.clear(); // 1차 캐시 비우고 DB에서 다시 읽게 함

        //then
        UserWallet reloaded = userWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updated).isEqualTo(0);
        assertThat(reloaded.getCrystalBalance()).isEqualTo(0L);
    }
}
