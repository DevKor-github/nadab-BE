package com.devkor.ifive.nadab.domain.wallet;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.application.WalletCommandService;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.builder.UserWalletBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class WalletServiceTest extends PostgresIntegrationTestSupport {

    @Autowired
    EntityManager em;

    @Autowired
    WalletCommandService walletCommandService;

    @Test
    void charge_crystal() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        long afterBalance = walletCommandService.chargeCrystal(user.getId(), 1000L,
                CrystalLogReason.DAILY_ANSWER_REWARD, "TEST", 1L);

        em.flush();  // 여기서 SQL 실행 강제
        em.clear();  // 1차 캐시 비우고 DB에서 다시 읽게 함

        UserWallet reloaded = em.createQuery(
                "select w from UserWallet w where w.user.id = :userId", UserWallet.class
        ).setParameter("userId", user.getId()).getSingleResult();


        //then
        assertThat(afterBalance).isEqualTo(1000L);
        assertThat(reloaded.getCrystalBalance()).isEqualTo(1000L);
    }

    @Test
    void consume_crystal() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        walletCommandService.chargeCrystal(user.getId(), 1000L,
                CrystalLogReason.DAILY_ANSWER_REWARD, "TEST", 1L);

        long afterBalance = walletCommandService.consumeCrystal(user.getId(), 500L,
                CrystalLogReason.REPORT_GENERATE_WEEKLY, "TEST", 2L);

        em.flush();  // 여기서 SQL 실행 강제
        em.clear();  // 1차 캐시 비우고 DB에서 다시 읽게 함

        UserWallet reloaded = em.createQuery(
                "select w from UserWallet w where w.user.id = :userId", UserWallet.class
        ).setParameter("userId", user.getId()).getSingleResult();


        //then
        assertThat(afterBalance).isEqualTo(500L);
        assertThat(reloaded.getCrystalBalance()).isEqualTo(500L);
    }

    @Test
    void consume_crystal_not_enough() {
        // given
        User user = new UserBuilder(em).build();
        UserWallet wallet = new UserWalletBuilder(em).user(user).build();

        // when
        walletCommandService.chargeCrystal(user.getId(), 300L,
                CrystalLogReason.DAILY_ANSWER_REWARD, "TEST", 1L);

        // then
        assertThrows(
                NotEnoughCrystalException.class,
                () -> walletCommandService.consumeCrystal(user.getId(), 500L,
                        CrystalLogReason.REPORT_GENERATE_WEEKLY, "TEST", 2L)
        );
    }
}
