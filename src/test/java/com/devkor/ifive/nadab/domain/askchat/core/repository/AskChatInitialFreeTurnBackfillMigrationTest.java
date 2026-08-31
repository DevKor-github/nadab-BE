package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatInitialFreeTurnBackfillMigrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    AskChatWalletRepository askChatWalletRepository;

    @Autowired
    AskChatWalletLogRepository askChatWalletLogRepository;

    @Autowired
    TestEntityManager em;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("classpath:db/migration/V20260831_1200__IS_repair_ask_chat_initial_free_turn_backfill.sql")
    Resource migration;

    @Test
    void backfill_grants_missing_initial_turns_once_and_preserves_existing_balances() throws IOException {
        User userWithoutWallet = new UserBuilder(em).build();

        User userWithWallet = new UserBuilder(em).build();
        askChatWalletRepository.save(AskChatWallet.create(userWithWallet, 0, 7));

        User alreadyGrantedUser = new UserBuilder(em).build();
        askChatWalletRepository.save(AskChatWallet.create(alreadyGrantedUser, 1, 5));
        askChatWalletLogRepository.save(AskChatWalletLog.createConfirmed(
                alreadyGrantedUser,
                null,
                null,
                3,
                0,
                3,
                0,
                AskChatWalletLogReason.INITIAL_FREE_GRANT,
                "SIGNUP",
                alreadyGrantedUser.getId(),
                "ask-chat-initial-free-" + alreadyGrantedUser.getId()
        ));
        em.flush();

        String migrationSql = migration.getContentAsString(StandardCharsets.UTF_8);
        jdbcTemplate.execute(migrationSql);
        jdbcTemplate.execute(migrationSql);
        em.clear();

        assertWalletBalances(userWithoutWallet.getId(), 3, 0);
        assertWalletBalances(userWithWallet.getId(), 3, 7);
        assertWalletBalances(alreadyGrantedUser.getId(), 1, 5);
        assertInitialGrantLog(userWithoutWallet.getId(), 3, 0);
        assertInitialGrantLog(userWithWallet.getId(), 3, 7);
        assertInitialGrantLog(alreadyGrantedUser.getId(), 3, 0);
    }

    private void assertWalletBalances(Long userId, int expectedFreeTurns, int expectedPaidTurns) {
        AskChatWallet wallet = askChatWalletRepository.findByUserId(userId).orElseThrow();
        assertThat(wallet.getFreeTurnBalance()).isEqualTo(expectedFreeTurns);
        assertThat(wallet.getPaidTurnBalance()).isEqualTo(expectedPaidTurns);
    }

    private void assertInitialGrantLog(Long userId, int expectedFreeBalance, int expectedPaidBalance) {
        List<AskChatWalletLog> initialGrantLogs = askChatWalletLogRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(log -> log.getReason() == AskChatWalletLogReason.INITIAL_FREE_GRANT)
                .toList();

        assertThat(initialGrantLogs).hasSize(1);
        AskChatWalletLog initialGrantLog = initialGrantLogs.getFirst();
        assertThat(initialGrantLog.getFreeTurnDelta()).isEqualTo(3);
        assertThat(initialGrantLog.getPaidTurnDelta()).isZero();
        assertThat(initialGrantLog.getFreeTurnBalanceAfter()).isEqualTo(expectedFreeBalance);
        assertThat(initialGrantLog.getPaidTurnBalanceAfter()).isEqualTo(expectedPaidBalance);
    }
}
