package com.devkor.ifive.nadab.domain.auth.infra;

import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles({"test", "local"})
@Import(LocalDummyUserRepository.class)
class LocalDummyUserRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    private LocalDummyUserRepository localDummyUserRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createIfAbsent_creates_dummy_user_interest_and_wallets() {
        Long userId = localDummyUserRepository.createIfAbsent();

        assertThat(userId).isEqualTo(11111L);
        assertThat(queryString("SELECT email FROM users WHERE id = 11111")).isEqualTo("test@example.com");
        assertThat(queryString("SELECT password_hash FROM users WHERE id = 11111")).isEqualTo("hashed_pw");
        assertThat(queryString("SELECT nickname FROM users WHERE id = 11111")).isEqualTo("TestUser");
        assertThat(queryString("SELECT default_profile_type FROM users WHERE id = 11111")).isEqualTo("DEFAULT");
        assertThat(queryString("SELECT signup_status FROM users WHERE id = 11111")).isEqualTo("PROFILE_INCOMPLETE");
        assertThat(queryString("""
                SELECT i.code
                  FROM user_interests ui
                  JOIN interests i ON i.id = ui.interest_id
                 WHERE ui.user_id = 11111
                """)).isEqualTo("ROUTINE");
        assertThat(queryLong(
                "SELECT crystal_balance FROM user_wallets WHERE user_id = 11111"
        )).isEqualTo(1_000L);
        assertThat(queryInteger(
                "SELECT free_turn_balance FROM ask_chat_wallets WHERE user_id = 11111"
        )).isEqualTo(3);
        assertThat(queryInteger(
                "SELECT paid_turn_balance FROM ask_chat_wallets WHERE user_id = 11111"
        )).isEqualTo(1_000);
        assertThat(queryLong(
                "SELECT version FROM ask_chat_wallets WHERE user_id = 11111"
        )).isZero();
    }

    @Test
    void createIfAbsent_does_not_reset_existing_wallet_balances() {
        localDummyUserRepository.createIfAbsent();
        jdbcTemplate.update(
                "UPDATE user_wallets SET crystal_balance = 900 WHERE user_id = 11111"
        );
        jdbcTemplate.update("""
                UPDATE ask_chat_wallets
                   SET free_turn_balance = 2,
                       paid_turn_balance = 900
                 WHERE user_id = 11111
                """);

        localDummyUserRepository.createIfAbsent();

        assertThat(queryLong(
                "SELECT COUNT(*) FROM users WHERE id = 11111"
        )).isEqualTo(1L);
        assertThat(queryLong(
                "SELECT COUNT(*) FROM user_interests WHERE user_id = 11111"
        )).isEqualTo(1L);
        assertThat(queryLong(
                "SELECT COUNT(*) FROM user_wallets WHERE user_id = 11111"
        )).isEqualTo(1L);
        assertThat(queryLong(
                "SELECT COUNT(*) FROM ask_chat_wallets WHERE user_id = 11111"
        )).isEqualTo(1L);
        assertThat(queryLong(
                "SELECT crystal_balance FROM user_wallets WHERE user_id = 11111"
        )).isEqualTo(900L);
        assertThat(queryInteger(
                "SELECT free_turn_balance FROM ask_chat_wallets WHERE user_id = 11111"
        )).isEqualTo(2);
        assertThat(queryInteger(
                "SELECT paid_turn_balance FROM ask_chat_wallets WHERE user_id = 11111"
        )).isEqualTo(900);
    }

    private String queryString(String sql) {
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    private Long queryLong(String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private Integer queryInteger(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
