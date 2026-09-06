package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class WithdrawalEventsMigrationTest extends PostgresIntegrationTestSupport {

    private static final String BACKFILL_START = "-- LEGACY BACKFILL START";
    private static final String BACKFILL_END = "-- LEGACY BACKFILL END";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager em;

    @Value("classpath:db/migration/V20260906_1200__IS_create_withdrawal_events.sql")
    private Resource migration;

    @Test
    void backfills_one_event_with_all_reasons_and_one_year_expiration() throws IOException {
        User user = new UserBuilder(em).build();
        em.flush();

        OffsetDateTime withdrawnAt = OffsetDateTime.parse("2026-09-01T12:30:00+09:00");
        insertLegacyReason(user.getId(), withdrawnAt, "DAILY_LOGGING_BURDEN", null);
        insertLegacyReason(user.getId(), withdrawnAt, "OTHER", "기타 의견");

        jdbcTemplate.execute(extractBackfillSql());

        Long eventId = jdbcTemplate.queryForObject(
                "SELECT id FROM withdrawal_events WHERE user_id = ?",
                Long.class,
                user.getId()
        );
        OffsetDateTime expiresAt = jdbcTemplate.queryForObject(
                "SELECT expires_at FROM withdrawal_events WHERE id = ?",
                OffsetDateTime.class,
                eventId
        );
        Long reasonCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM withdrawal_event_reasons WHERE event_id = ?",
                Long.class,
                eventId
        );

        assertThat(expiresAt).isEqualTo(withdrawnAt.plusYears(1));
        assertThat(reasonCount).isEqualTo(2L);
    }

    @Test
    void preserves_event_and_reasons_when_user_is_hard_deleted() {
        User user = new UserBuilder(em).build();
        em.flush();

        OffsetDateTime withdrawnAt = OffsetDateTime.parse("2026-09-01T12:30:00+09:00");
        Long eventId = insertEvent(user.getId(), withdrawnAt);
        jdbcTemplate.update("""
                INSERT INTO withdrawal_event_reasons (event_id, reason)
                VALUES (?, 'DAILY_LOGGING_BURDEN')
                """, eventId);

        jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());

        Long linkedUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM withdrawal_events WHERE id = ?",
                Long.class,
                eventId
        );
        Long reasonCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM withdrawal_event_reasons WHERE event_id = ?",
                Long.class,
                eventId
        );

        assertThat(linkedUserId).isNull();
        assertThat(reasonCount).isEqualTo(1L);
    }

    @Test
    void rejects_duplicate_reason_for_same_event() {
        User user = new UserBuilder(em).build();
        em.flush();

        OffsetDateTime withdrawnAt = OffsetDateTime.parse("2026-09-01T12:30:00+09:00");
        Long eventId = insertEvent(user.getId(), withdrawnAt);
        jdbcTemplate.update("""
                INSERT INTO withdrawal_event_reasons (event_id, reason)
                VALUES (?, 'DAILY_LOGGING_BURDEN')
                """, eventId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO withdrawal_event_reasons (event_id, reason)
                VALUES (?, 'DAILY_LOGGING_BURDEN')
                """, eventId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejects_expiration_not_after_withdrawal() {
        User user = new UserBuilder(em).build();
        em.flush();

        OffsetDateTime withdrawnAt = OffsetDateTime.parse("2026-09-01T12:30:00+09:00");

        assertThatThrownBy(() -> insertEvent(user.getId(), withdrawnAt, withdrawnAt))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertLegacyReason(
            Long userId,
            OffsetDateTime withdrawnAt,
            String reason,
            String customReason
    ) {
        jdbcTemplate.update("""
                INSERT INTO user_withdrawal_reasons (
                    user_id,
                    reason,
                    custom_reason,
                    withdrawn_at
                )
                VALUES (?, ?, ?, ?)
                """, userId, reason, customReason, withdrawnAt);
    }

    private Long insertEvent(Long userId, OffsetDateTime withdrawnAt) {
        return insertEvent(userId, withdrawnAt, withdrawnAt.plusYears(1));
    }

    private Long insertEvent(Long userId, OffsetDateTime withdrawnAt, OffsetDateTime expiresAt) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO withdrawal_events (
                    user_id,
                    withdrawn_at,
                    expires_at
                )
                VALUES (?, ?, ?)
                RETURNING id
                """, Long.class, userId, withdrawnAt, expiresAt);
    }

    private String extractBackfillSql() throws IOException {
        String migrationSql = migration.getContentAsString(StandardCharsets.UTF_8);
        int start = migrationSql.indexOf(BACKFILL_START) + BACKFILL_START.length();
        int end = migrationSql.indexOf(BACKFILL_END);
        return migrationSql.substring(start, end);
    }
}
