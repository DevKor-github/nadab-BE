package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagDocumentStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagReferenceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSourceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyWalletStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatWalletSummaryDto;
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

    @Test
    void aggregates_wallet_and_rag_statuses_by_seoul_date() {
        Long firstUserId = insertUser("ask-chat-billing-rag-first");
        Long secondUserId = insertUser("ask-chat-billing-rag-second");
        Long firstSessionId = insertSession(firstUserId, "ACTIVE", utc(2026, 8, 10, 13, 0));
        Long secondSessionId = insertSession(secondUserId, "ACTIVE", utc(2026, 8, 11, 13, 0));
        Long firstMessageId = insertMessageWithId(firstSessionId, utc(2026, 8, 10, 13, 1));
        Long secondMessageId = insertMessageWithId(secondSessionId, utc(2026, 8, 11, 13, 1));

        insertWalletLog(firstUserId, firstSessionId, 7, 0, 7, 0,
                "INITIAL_FREE_GRANT", "CONFIRMED", utc(2026, 8, 10, 13, 2));
        insertWalletLog(firstUserId, firstSessionId, -1, 0, 6, 0,
                "ANSWER_SUCCESS_CONSUME", "CONFIRMED", utc(2026, 8, 10, 13, 3));
        insertWalletLog(firstUserId, firstSessionId, 0, -1, 6, 0,
                "ANSWER_SUCCESS_CONSUME", "CONFIRMED", utc(2026, 8, 10, 13, 4));
        insertWalletLog(firstUserId, firstSessionId, -1, 0, 6, 0,
                "ANSWER_SUCCESS_CONSUME", "PENDING", utc(2026, 8, 10, 13, 5));
        insertWalletLog(secondUserId, secondSessionId, 0, -1, 0, 0,
                "ANSWER_SUCCESS_CONSUME", "REFUNDED", utc(2026, 8, 11, 13, 2));
        insertWalletLog(secondUserId, secondSessionId, 0, 1, 0, 1,
                "ANSWER_FAILURE_REFUND", "CONFIRMED", utc(2026, 8, 11, 13, 3));
        insertWalletLog(secondUserId, secondSessionId, 0, 10, 0, 11,
                "CRYSTAL_CHARGE", "CONFIRMED", utc(2026, 8, 11, 13, 4));

        Long completedMessageDocumentId = insertRagDocument(
                firstUserId, "ASK_CHAT_MESSAGE", firstMessageId, "COMPLETED", 0, null,
                utc(2026, 8, 10, 13, 6)
        );
        Long failedAnswerDocumentId = insertRagDocument(
                firstUserId, "ANSWER_ENTRY", 1001L, "FAILED", 2, "EMBEDDING_TIMEOUT",
                utc(2026, 8, 10, 13, 7)
        );
        Long deadLetterDocumentId = insertRagDocument(
                secondUserId, "ASK_CHAT_MESSAGE", secondMessageId, "DEAD_LETTER", 3, null,
                utc(2026, 8, 11, 13, 6)
        );
        insertRagDocument(
                secondUserId, "ANSWER_ENTRY", 1002L, "COMPLETED", 0, null,
                utc(2026, 8, 11, 15, 1)
        );
        insertReference(firstMessageId, completedMessageDocumentId, 0, utc(2026, 8, 10, 13, 8));
        insertReference(firstMessageId, failedAnswerDocumentId, 1, utc(2026, 8, 10, 13, 9));
        insertReference(secondMessageId, completedMessageDocumentId, 0, utc(2026, 8, 11, 13, 8));

        OffsetDateTime startInclusive = utc(2026, 8, 9, 15, 0);
        OffsetDateTime endExclusive = utc(2026, 8, 11, 15, 0);

        List<AskChatDailyWalletStatsDto> dailyWalletStats = repository.findDailyWalletStats(
                startInclusive,
                endExclusive
        );
        AskChatWalletSummaryDto walletSummary = repository.findWalletSummary(startInclusive, endExclusive);
        List<AskChatDailyRagDocumentStatsDto> dailyDocuments = repository.findDailyRagDocumentStats(
                startInclusive,
                endExclusive
        );
        List<AskChatDailyRagReferenceStatsDto> dailyReferences = repository.findDailyRagReferenceStats(
                startInclusive,
                endExclusive
        );
        AskChatRagSummaryDto ragSummary = repository.findRagSummary(startInclusive, endExclusive);
        List<AskChatRagSourceStatsDto> sourceStats = repository.findRagSourceStats(startInclusive, endExclusive);
        List<AskChatRagErrorStatsDto> ragErrors = repository.findRagErrorStats(startInclusive, endExclusive);

        assertThat(dailyWalletStats.get(0))
                .extracting(
                        AskChatDailyWalletStatsDto::totalLogCount,
                        AskChatDailyWalletStatsDto::pendingLogCount,
                        AskChatDailyWalletStatsDto::confirmedLogCount,
                        AskChatDailyWalletStatsDto::freeTurnsGranted,
                        AskChatDailyWalletStatsDto::freeTurnsConsumed,
                        AskChatDailyWalletStatsDto::paidTurnsConsumed,
                        AskChatDailyWalletStatsDto::netFreeTurnDelta,
                        AskChatDailyWalletStatsDto::netPaidTurnDelta
                )
                .containsExactly(4L, 1L, 3L, 7L, 1L, 1L, 5L, -1L);
        assertThat(dailyWalletStats.get(1))
                .extracting(
                        AskChatDailyWalletStatsDto::totalLogCount,
                        AskChatDailyWalletStatsDto::refundedLogCount,
                        AskChatDailyWalletStatsDto::paidTurnsRefunded,
                        AskChatDailyWalletStatsDto::paidTurnsCharged,
                        AskChatDailyWalletStatsDto::netPaidTurnDelta
                )
                .containsExactly(3L, 1L, 1L, 10L, 10L);
        assertThat(walletSummary).isEqualTo(
                new AskChatWalletSummaryDto(7L, 1L, 5L, 1L, 7L, 1L, 1L, 0L, 1L, 10L, 5L, 9L)
        );

        assertThat(dailyDocuments.get(0))
                .extracting(
                        AskChatDailyRagDocumentStatsDto::date,
                        AskChatDailyRagDocumentStatsDto::totalDocumentCount,
                        AskChatDailyRagDocumentStatsDto::completedDocumentCount,
                        AskChatDailyRagDocumentStatsDto::failedDocumentCount,
                        AskChatDailyRagDocumentStatsDto::averageFailedRetryCount
                )
                .containsExactly(LocalDate.of(2026, 8, 10), 2L, 1L, 1L, 2.0);
        assertThat(dailyDocuments.get(1).deadLetterDocumentCount()).isEqualTo(1L);
        assertThat(dailyReferences).containsExactly(
                new AskChatDailyRagReferenceStatsDto(LocalDate.of(2026, 8, 10), 2L, 2L),
                new AskChatDailyRagReferenceStatsDto(LocalDate.of(2026, 8, 11), 1L, 1L)
        );
        assertThat(ragSummary).isEqualTo(new AskChatRagSummaryDto(3L, 0L, 1L, 1L, 1L, 2.5, 3L, 2L));
        assertThat(sourceStats).containsExactly(
                new AskChatRagSourceStatsDto("ASK_CHAT_MESSAGE", 2L, 0L, 1L, 0L, 1L),
                new AskChatRagSourceStatsDto("ANSWER_ENTRY", 1L, 0L, 0L, 1L, 0L)
        );
        assertThat(ragErrors).containsExactly(
                new AskChatRagErrorStatsDto("EMBEDDING_TIMEOUT", 1L),
                new AskChatRagErrorStatsDto("UNKNOWN", 1L)
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

    private Long insertMessageWithId(Long sessionId, OffsetDateTime createdAt) {
        return jdbcTemplate.queryForObject("""
                insert into ask_chat_messages (session_id, role, status, content, created_at)
                values (?, 'ASSISTANT', 'COMPLETED', 'reference message', ?)
                returning id
                """, Long.class, sessionId, createdAt);
    }

    private void insertWalletLog(
            Long userId,
            Long sessionId,
            int freeTurnDelta,
            int paidTurnDelta,
            int freeTurnBalanceAfter,
            int paidTurnBalanceAfter,
            String reason,
            String status,
            OffsetDateTime createdAt
    ) {
        jdbcTemplate.update("""
                insert into ask_chat_wallet_logs (
                    user_id, session_id, free_turn_delta, paid_turn_delta,
                    free_turn_balance_after, paid_turn_balance_after,
                    reason, status, created_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, userId, sessionId, freeTurnDelta, paidTurnDelta,
                freeTurnBalanceAfter, paidTurnBalanceAfter, reason, status, createdAt);
    }

    private Long insertRagDocument(
            Long userId,
            String sourceType,
            Long sourceId,
            String embeddingStatus,
            int retryCount,
            String errorCode,
            OffsetDateTime createdAt
    ) {
        return jdbcTemplate.queryForObject("""
                insert into ask_chat_rag_documents (
                    user_id, source_type, source_id, content, metadata,
                    embedding_model, embedding_version, embedding_status,
                    error_code, retry_count, created_at, updated_at
                )
                values (?, ?, ?, 'rag content', '{}'::jsonb, ?, 1, ?, ?, ?, ?, ?)
                returning id
                """, Long.class, userId, sourceType, sourceId,
                "text-embedding-3-small", embeddingStatus, errorCode, retryCount, createdAt, createdAt);
    }

    private void insertReference(
            Long messageId,
            Long ragDocumentId,
            int displayOrder,
            OffsetDateTime createdAt
    ) {
        jdbcTemplate.update("""
                insert into ask_chat_message_references (message_id, rag_document_id, display_order, created_at)
                values (?, ?, ?, ?)
                """, messageId, ragDocumentId, displayOrder, createdAt);
    }

    private OffsetDateTime utc(int year, int month, int day, int hour, int minute) {
        return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
    }
}
