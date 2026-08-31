package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AskChatStatsRepository.class)
class AskChatStatsRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    private AskChatStatsRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void aggregates_usage_and_quality_by_seoul_date_with_half_open_range() {
        Long firstUserId = insertUser("ask-chat-stats-first");
        Long secondUserId = insertUser("ask-chat-stats-second");

        Long firstSessionId = insertSession(
                firstUserId,
                "ACTIVE",
                utc(2026, 8, 10, 14, 0)
        );
        Long secondSessionId = insertSession(
                firstUserId,
                "ENDED",
                utc(2026, 8, 10, 15, 30)
        );
        insertSession(secondUserId, "ACTIVE", utc(2026, 8, 11, 14, 30));
        Long outsideSessionId = insertSession(secondUserId, "ENDED", utc(2026, 8, 11, 15, 30));

        insertMessage(firstSessionId, "USER", "COMPLETED", null, utc(2026, 8, 10, 14, 10));
        insertMessage(firstSessionId, "ASSISTANT", "COMPLETED", 100L, utc(2026, 8, 10, 14, 11));
        insertMessage(firstSessionId, "ASSISTANT", "COMPLETED", 300L, utc(2026, 8, 10, 14, 12));
        insertMessage(firstSessionId, "ASSISTANT", "FAILED", 50L, utc(2026, 8, 10, 14, 13), "TIMEOUT");
        insertMessage(firstSessionId, "ASSISTANT", "FAILED", 60L, utc(2026, 8, 10, 14, 14), null);
        insertMessage(secondSessionId, "USER", "COMPLETED", null, utc(2026, 8, 10, 15, 31));
        insertMessage(secondSessionId, "ASSISTANT", "COMPLETED", 200L, utc(2026, 8, 10, 15, 32));
        insertMessage(outsideSessionId, "USER", "COMPLETED", null, utc(2026, 8, 11, 15, 31));

        OffsetDateTime startInclusive = utc(2026, 8, 9, 15, 0);
        OffsetDateTime endExclusive = utc(2026, 8, 11, 15, 0);

        List<AskChatDailySessionStatsDto> dailySessions = repository.findDailySessionStats(
                startInclusive,
                endExclusive
        );
        List<AskChatDailyMessageStatsDto> dailyMessages = repository.findDailyMessageStats(
                startInclusive,
                endExclusive
        );
        AskChatSessionSummaryDto sessionSummary = repository.findSessionSummary(
                startInclusive,
                endExclusive
        );
        AskChatMessageSummaryDto messageSummary = repository.findMessageSummary(
                startInclusive,
                endExclusive
        );
        List<AskChatErrorStatsDto> errors = repository.findAssistantErrorStats(
                startInclusive,
                endExclusive
        );

        assertThat(dailySessions)
                .extracting(AskChatDailySessionStatsDto::date)
                .containsExactly(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 11));
        assertThat(dailySessions.get(0))
                .extracting(
                        AskChatDailySessionStatsDto::sessionCount,
                        AskChatDailySessionStatsDto::uniqueUserCount,
                        AskChatDailySessionStatsDto::activeSessionCount,
                        AskChatDailySessionStatsDto::endedSessionCount
                )
                .containsExactly(1L, 1L, 1L, 0L);
        assertThat(dailySessions.get(1))
                .extracting(
                        AskChatDailySessionStatsDto::sessionCount,
                        AskChatDailySessionStatsDto::uniqueUserCount,
                        AskChatDailySessionStatsDto::activeSessionCount,
                        AskChatDailySessionStatsDto::endedSessionCount
                )
                .containsExactly(2L, 2L, 1L, 1L);

        assertThat(dailyMessages.get(0).date()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(dailyMessages.get(0).userMessageCount()).isEqualTo(1L);
        assertThat(dailyMessages.get(0).completedAssistantMessageCount()).isEqualTo(2L);
        assertThat(dailyMessages.get(0).failedAssistantMessageCount()).isEqualTo(2L);
        assertThat(dailyMessages.get(0).averageGenerationDurationMs()).isEqualTo(200.0);
        assertThat(dailyMessages.get(0).p95GenerationDurationMs()).isEqualTo(290L);
        assertThat(dailyMessages.get(1).userMessageCount()).isEqualTo(1L);
        assertThat(dailyMessages.get(1).completedAssistantMessageCount()).isEqualTo(1L);

        assertThat(sessionSummary).isEqualTo(new AskChatSessionSummaryDto(3L, 2L, 2L, 1L));
        assertThat(messageSummary).isEqualTo(new AskChatMessageSummaryDto(2L, 3L, 2L, 200.0, 290L));
        assertThat(errors).containsExactly(
                new AskChatErrorStatsDto("TIMEOUT", 1L),
                new AskChatErrorStatsDto("UNKNOWN", 1L)
        );
    }

    private Long insertUser(String prefix) {
        return jdbcTemplate.queryForObject("""
                insert into users (email, password_hash, nickname, signup_status)
                values (?, ?, ?, ?)
                returning id
                """, Long.class, prefix + "@test.com", "hashed", prefix, "COMPLETED");
    }

    private Long insertSession(Long userId, String status, OffsetDateTime createdAt) {
        return jdbcTemplate.queryForObject("""
                insert into ask_chat_sessions (user_id, status, answered_turn_count, created_at, updated_at)
                values (?, ?, 0, ?, ?)
                returning id
                """, Long.class, userId, status, createdAt, createdAt);
    }

    private void insertMessage(
            Long sessionId,
            String role,
            String status,
            Long generationDurationMs,
            OffsetDateTime createdAt
    ) {
        insertMessage(sessionId, role, status, generationDurationMs, createdAt, null);
    }

    private void insertMessage(
            Long sessionId,
            String role,
            String status,
            Long generationDurationMs,
            OffsetDateTime createdAt,
            String errorCode
    ) {
        jdbcTemplate.update("""
                insert into ask_chat_messages (
                    session_id, role, status, content, generation_duration_ms, error_code, created_at
                )
                values (?, ?, ?, 'test message', ?, ?, ?)
                """, sessionId, role, status, generationDurationMs, errorCode, createdAt);
    }

    private OffsetDateTime utc(int year, int month, int day, int hour, int minute) {
        return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
    }
}
