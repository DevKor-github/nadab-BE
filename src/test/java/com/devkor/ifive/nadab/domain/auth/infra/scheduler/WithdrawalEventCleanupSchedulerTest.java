package com.devkor.ifive.nadab.domain.auth.infra.scheduler;

import com.devkor.ifive.nadab.domain.auth.core.repository.WithdrawalEventRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(WithdrawalEventCleanupScheduler.class)
class WithdrawalEventCleanupSchedulerTest extends PostgresIntegrationTestSupport {

    @MockitoSpyBean
    WithdrawalEventRepository withdrawalEventRepository;

    @Autowired
    WithdrawalEventCleanupScheduler scheduler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final List<Long> eventIds = new ArrayList<>();
    private final List<Long> userIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        eventIds.forEach(eventId -> jdbcTemplate.update(
                "DELETE FROM withdrawal_events WHERE id = ?",
                eventId
        ));
        userIds.forEach(userId -> jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                userId
        ));
    }

    @Test
    void anonymizes_old_events_deletes_expired_events_and_is_idempotent() {
        OffsetDateTime now = OffsetDateTime.now();
        Long oldUserId = createUser();
        Long recentUserId = createUser();
        Long expiredUserId = createUser();

        Long oldEventId = insertEvent(oldUserId, now.minusDays(15), now.plusDays(350));
        insertReason(oldEventId, "OTHER", "오래된 자유 입력 사유");
        insertLegacyReason(oldUserId, now.minusDays(15), "OTHER", "오래된 레거시 사유");

        Long recentEventId = insertEvent(recentUserId, now.minusDays(13), now.plusDays(352));
        insertReason(recentEventId, "OTHER", "최근 자유 입력 사유");
        insertLegacyReason(recentUserId, now.minusDays(13), "OTHER", "최근 레거시 사유");

        Long expiredEventId = insertEvent(expiredUserId, now.minusYears(2), now.minusDays(1));
        insertReason(expiredEventId, "DAILY_LOGGING_BURDEN", null);

        scheduler.cleanupWithdrawalEvents();

        OffsetDateTime firstAnonymizedAt = queryAnonymizedAt(oldEventId);
        assertThat(queryUserId(oldEventId)).isNull();
        assertThat(firstAnonymizedAt).isNotNull();
        assertThat(queryCustomReason(oldEventId)).isNull();
        assertThat(countLegacyReasons(oldUserId)).isZero();

        assertThat(queryUserId(recentEventId)).isEqualTo(recentUserId);
        assertThat(queryAnonymizedAt(recentEventId)).isNull();
        assertThat(queryCustomReason(recentEventId)).isEqualTo("최근 자유 입력 사유");
        assertThat(countLegacyReasons(recentUserId)).isEqualTo(1L);

        assertThat(countEvents(expiredEventId)).isZero();
        assertThat(countReasons(expiredEventId)).isZero();

        scheduler.cleanupWithdrawalEvents();

        assertThat(queryAnonymizedAt(oldEventId)).isEqualTo(firstAnonymizedAt);
        assertThat(countEvents(oldEventId)).isEqualTo(1L);
        assertThat(countReasons(oldEventId)).isEqualTo(1L);
        assertThat(countLegacyReasons(recentUserId)).isEqualTo(1L);
    }

    @Test
    void failure_rolls_back_expiration_and_anonymization_changes_together() {
        OffsetDateTime now = OffsetDateTime.now();
        Long oldUserId = createUser();
        Long expiredUserId = createUser();

        Long oldEventId = insertEvent(oldUserId, now.minusDays(15), now.plusDays(350));
        insertReason(oldEventId, "OTHER", "롤백할 자유 입력 사유");
        insertLegacyReason(oldUserId, now.minusDays(15), "OTHER", "롤백할 레거시 사유");

        Long expiredEventId = insertEvent(expiredUserId, now.minusYears(2), now.minusDays(1));
        insertReason(expiredEventId, "APP_ERROR_OR_SLOWNESS", null);

        doThrow(new IllegalStateException("탈퇴 이벤트 비식별화 실패"))
                .when(withdrawalEventRepository)
                .anonymizeEventsWithdrawnBefore(any(OffsetDateTime.class), any(OffsetDateTime.class));

        assertThatThrownBy(scheduler::cleanupWithdrawalEvents)
                .isInstanceOf(IllegalStateException.class);

        assertThat(countEvents(expiredEventId)).isEqualTo(1L);
        assertThat(countReasons(expiredEventId)).isEqualTo(1L);
        assertThat(queryUserId(oldEventId)).isEqualTo(oldUserId);
        assertThat(queryAnonymizedAt(oldEventId)).isNull();
        assertThat(queryCustomReason(oldEventId)).isEqualTo("롤백할 자유 입력 사유");
        assertThat(countLegacyReasons(oldUserId)).isEqualTo(1L);
    }

    private Long createUser() {
        User user = User.createUser(
                "withdrawal-cleanup+" + System.nanoTime() + "@test.com",
                "hashed_password"
        );
        user.updateNickname("wcu" + System.nanoTime());
        Long userId = userRepository.save(user).getId();
        userIds.add(userId);
        return userId;
    }

    private Long insertEvent(Long userId, OffsetDateTime withdrawnAt, OffsetDateTime expiresAt) {
        Long eventId = jdbcTemplate.queryForObject("""
                INSERT INTO withdrawal_events (
                    user_id,
                    withdrawn_at,
                    expires_at
                )
                VALUES (?, ?, ?)
                RETURNING id
                """, Long.class, userId, withdrawnAt, expiresAt);
        eventIds.add(eventId);
        return eventId;
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
}
