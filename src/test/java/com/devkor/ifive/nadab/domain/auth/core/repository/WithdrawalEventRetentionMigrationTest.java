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
class WithdrawalEventRetentionMigrationTest extends PostgresIntegrationTestSupport {

    private static final String CLEANUP_START = "-- RETENTION CLEANUP START";
    private static final String CLEANUP_END = "-- RETENTION CLEANUP END";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TestEntityManager em;

    @Value("classpath:db/migration/V20260906_1300__IS_add_withdrawal_event_retention_cleanup.sql")
    Resource migration;

    @Test
    void cleans_existing_events_by_anonymization_and_expiration_deadlines() throws IOException {
        User oldUser = new UserBuilder(em).build();
        User recentUser = new UserBuilder(em).build();
        User expiredUser = new UserBuilder(em).build();
        em.flush();

        OffsetDateTime now = OffsetDateTime.now();
        Long oldEventId = insertEvent(
                oldUser.getId(),
                now.minusDays(30),
                now.plusDays(335)
        );
        insertReason(oldEventId, "OTHER", "오래된 자유 입력 사유");
        insertLegacyReason(oldUser.getId(), now.minusDays(30), "OTHER", "오래된 레거시 사유");

        Long recentEventId = insertEvent(
                recentUser.getId(),
                now.minusDays(13),
                now.plusDays(352)
        );
        insertReason(recentEventId, "OTHER", "최근 자유 입력 사유");
        insertLegacyReason(recentUser.getId(), now.minusDays(13), "OTHER", "최근 레거시 사유");

        Long expiredEventId = insertEvent(
                expiredUser.getId(),
                now.minusYears(2),
                now.minusYears(1)
        );
        insertReason(expiredEventId, "APP_ERROR_OR_SLOWNESS", null);

        jdbcTemplate.execute(extractCleanupSql());

        assertThat(queryUserId(oldEventId)).isNull();
        assertThat(queryAnonymizedAt(oldEventId)).isNotNull();
        assertThat(queryCustomReason(oldEventId)).isNull();
        assertThat(countReasons(oldEventId)).isEqualTo(1L);
        assertThat(countLegacyReasons(oldUser.getId())).isZero();

        assertThat(queryUserId(recentEventId)).isEqualTo(recentUser.getId());
        assertThat(queryAnonymizedAt(recentEventId)).isNull();
        assertThat(queryCustomReason(recentEventId)).isEqualTo("최근 자유 입력 사유");
        assertThat(countLegacyReasons(recentUser.getId())).isEqualTo(1L);

        assertThat(countEvents(expiredEventId)).isZero();
        assertThat(countReasons(expiredEventId)).isZero();
    }

    @Test
    void allows_other_reason_without_custom_text_after_anonymization() {
        OffsetDateTime withdrawnAt = OffsetDateTime.now().minusDays(30);
        Long eventId = insertEvent(null, withdrawnAt, withdrawnAt.plusYears(1));
        jdbcTemplate.update(
                "UPDATE withdrawal_events SET anonymized_at = ? WHERE id = ?",
                OffsetDateTime.now(),
                eventId
        );

        insertReason(eventId, "OTHER", null);

        assertThat(countReasons(eventId)).isEqualTo(1L);
    }

    @Test
    void still_rejects_blank_other_reason() {
        OffsetDateTime withdrawnAt = OffsetDateTime.now();
        Long eventId = insertEvent(null, withdrawnAt, withdrawnAt.plusYears(1));

        assertThatThrownBy(() -> insertReason(eventId, "OTHER", "   "))
                .isInstanceOf(DataIntegrityViolationException.class);
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

    private void insertReason(Long eventId, String reason, String customReason) {
        jdbcTemplate.update("""
                INSERT INTO withdrawal_event_reasons (
                    event_id,
                    reason,
                    custom_reason
                )
                VALUES (?, ?, ?)
                """, eventId, reason, customReason);
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

    private Long queryUserId(Long eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT user_id FROM withdrawal_events WHERE id = ?",
                Long.class,
                eventId
        );
    }

    private OffsetDateTime queryAnonymizedAt(Long eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT anonymized_at FROM withdrawal_events WHERE id = ?",
                OffsetDateTime.class,
                eventId
        );
    }

    private String queryCustomReason(Long eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT custom_reason FROM withdrawal_event_reasons WHERE event_id = ?",
                String.class,
                eventId
        );
    }

    private Long countEvents(Long eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM withdrawal_events WHERE id = ?",
                Long.class,
                eventId
        );
    }

    private Long countReasons(Long eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM withdrawal_event_reasons WHERE event_id = ?",
                Long.class,
                eventId
        );
    }

    private Long countLegacyReasons(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_withdrawal_reasons WHERE user_id = ?",
                Long.class,
                userId
        );
    }

    private String extractCleanupSql() throws IOException {
        String migrationSql = migration.getContentAsString(StandardCharsets.UTF_8);
        int start = migrationSql.indexOf(CLEANUP_START) + CLEANUP_START.length();
        int end = migrationSql.indexOf(CLEANUP_END);
        return migrationSql.substring(start, end);
    }
}
