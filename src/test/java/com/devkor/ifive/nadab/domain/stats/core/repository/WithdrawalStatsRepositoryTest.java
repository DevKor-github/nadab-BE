package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(WithdrawalStatsRepository.class)
class WithdrawalStatsRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    WithdrawalStatsRepository repository;

    @Autowired
    TestEntityManager em;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void keeps_hard_deleted_user_event_in_stats_without_custom_reason() {
        User user = new UserBuilder(em).build();
        em.flush();

        OffsetDateTime withdrawnAt = OffsetDateTime.now().minusDays(30);
        Long eventId = insertEvent(user.getId(), withdrawnAt, withdrawnAt.plusYears(1));
        insertReason(eventId, "DAILY_LOGGING_BURDEN", null);
        insertReason(eventId, "OTHER", "자유 입력 사유");

        jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
        em.clear();

        List<Object[]> latestRows = repository.findLatestRetainedWithdrawalReasonRows(100);
        Map<String, Long> reasonCounts = toReasonCountMap(repository.countRetainedWithdrawalReasons());

        assertThat(latestRows).hasSize(2);
        assertThat(latestRows).extracting(row -> ((Number) row[0]).longValue()).containsOnly(eventId);
        assertThat(latestRows).extracting(row -> String.valueOf(row[2]))
                .containsExactly("DAILY_LOGGING_BURDEN", "OTHER");
        assertThat(latestRows).extracting(row -> row[3]).containsOnlyNulls();
        assertThat(reasonCounts).containsExactlyInAnyOrderEntriesOf(Map.of(
                "DAILY_LOGGING_BURDEN", 1L,
                "OTHER", 1L
        ));
    }

    @Test
    void limits_latest_rows_by_event_and_excludes_expired_events() {
        OffsetDateTime now = OffsetDateTime.now();
        Long olderRetainedEventId = insertEvent(null, now.minusDays(30), now.plusDays(30));
        insertReason(olderRetainedEventId, "DAILY_LOGGING_BURDEN", null);

        Long latestRetainedEventId = insertEvent(null, now.minusDays(1), now.plusDays(364));
        insertReason(latestRetainedEventId, "OTHER", "최근 자유 입력 사유");
        insertReason(latestRetainedEventId, "PRIVACY_RECORD_CONCERN", null);

        Long expiredEventId = insertEvent(null, now.minusDays(3), now.minusDays(2));
        insertReason(expiredEventId, "APP_ERROR_OR_SLOWNESS", null);

        List<Object[]> latestRows = repository.findLatestRetainedWithdrawalReasonRows(1);
        Map<String, Long> reasonCounts = toReasonCountMap(repository.countRetainedWithdrawalReasons());

        assertThat(latestRows).hasSize(2);
        assertThat(latestRows)
                .extracting(row -> ((Number) row[0]).longValue())
                .containsOnly(latestRetainedEventId);
        assertThat(reasonCounts).containsExactlyInAnyOrderEntriesOf(Map.of(
                "DAILY_LOGGING_BURDEN", 1L,
                "OTHER", 1L,
                "PRIVACY_RECORD_CONCERN", 1L
        ));
        assertThat(reasonCounts).doesNotContainKey("APP_ERROR_OR_SLOWNESS");
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

    private Map<String, Long> toReasonCountMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> String.valueOf(row[0]),
                row -> ((Number) row[1]).longValue()
        ));
    }
}
