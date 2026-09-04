package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagDocumentStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagReferenceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyWalletStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSourceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatWalletSummaryDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AskChatStatsRepository {

    private final EntityManager em;

    public List<AskChatDailySessionStatsDto> findDailySessionStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    (s.created_at at time zone 'Asia/Seoul')::date as stats_date,
                    count(*) as session_count,
                    count(distinct s.user_id) as unique_user_count,
                    count(*) filter (where s.status = 'ACTIVE') as active_session_count,
                    count(*) filter (where s.status = 'ENDED') as ended_session_count
                from ask_chat_sessions s
                where s.created_at >= :startInclusive
                  and s.created_at < :endExclusive
                group by 1
                order by 1
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatDailySessionStatsDto(
                        toLocalDate(row[0]),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toLong(row[4])
                ))
                .toList();
    }

    public AskChatSessionSummaryDto findSessionSummary(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        Object[] row = (Object[]) em.createNativeQuery("""
                select
                    count(*) as session_count,
                    count(distinct s.user_id) as unique_user_count,
                    count(*) filter (where s.status = 'ACTIVE') as active_session_count,
                    count(*) filter (where s.status = 'ENDED') as ended_session_count
                from ask_chat_sessions s
                where s.created_at >= :startInclusive
                  and s.created_at < :endExclusive
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getSingleResult();

        return new AskChatSessionSummaryDto(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3])
        );
    }

    public List<AskChatDailyMessageStatsDto> findDailyMessageStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    (m.created_at at time zone 'Asia/Seoul')::date as stats_date,
                    count(*) filter (where m.role = 'USER') as user_message_count,
                    count(*) filter (where m.role = 'ASSISTANT' and m.status = 'COMPLETED')
                        as completed_assistant_message_count,
                    count(*) filter (where m.role = 'ASSISTANT' and m.status = 'FAILED')
                        as failed_assistant_message_count,
                    avg(m.generation_duration_ms) filter (
                        where m.role = 'ASSISTANT'
                          and m.status = 'COMPLETED'
                          and m.generation_duration_ms is not null
                    ) as average_generation_duration_ms,
                    percentile_cont(0.95) within group (order by m.generation_duration_ms) filter (
                        where m.role = 'ASSISTANT'
                          and m.status = 'COMPLETED'
                          and m.generation_duration_ms is not null
                    ) as p95_generation_duration_ms
                from ask_chat_messages m
                where m.created_at >= :startInclusive
                  and m.created_at < :endExclusive
                group by 1
                order by 1
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatDailyMessageStatsDto(
                        toLocalDate(row[0]),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toDouble(row[4]),
                        Math.round(toDouble(row[5]))
                ))
                .toList();
    }

    public AskChatMessageSummaryDto findMessageSummary(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        Object[] row = (Object[]) em.createNativeQuery("""
                select
                    count(*) filter (where m.role = 'USER') as user_message_count,
                    count(*) filter (where m.role = 'ASSISTANT' and m.status = 'COMPLETED')
                        as completed_assistant_message_count,
                    count(*) filter (where m.role = 'ASSISTANT' and m.status = 'FAILED')
                        as failed_assistant_message_count,
                    avg(m.generation_duration_ms) filter (
                        where m.role = 'ASSISTANT'
                          and m.status = 'COMPLETED'
                          and m.generation_duration_ms is not null
                    ) as average_generation_duration_ms,
                    percentile_cont(0.95) within group (order by m.generation_duration_ms) filter (
                        where m.role = 'ASSISTANT'
                          and m.status = 'COMPLETED'
                          and m.generation_duration_ms is not null
                    ) as p95_generation_duration_ms
                from ask_chat_messages m
                where m.created_at >= :startInclusive
                  and m.created_at < :endExclusive
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getSingleResult();

        return new AskChatMessageSummaryDto(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toDouble(row[3]),
                Math.round(toDouble(row[4]))
        );
    }

    public List<AskChatErrorStatsDto> findAssistantErrorStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    coalesce(nullif(trim(m.error_code), ''), 'UNKNOWN') as error_code,
                    count(*) as error_count
                from ask_chat_messages m
                where m.created_at >= :startInclusive
                  and m.created_at < :endExclusive
                  and m.role = 'ASSISTANT'
                  and m.status = 'FAILED'
                group by 1
                order by error_count desc, error_code asc
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatErrorStatsDto(String.valueOf(row[0]), toLong(row[1])))
                .toList();
    }

    public List<AskChatDailyWalletStatsDto> findDailyWalletStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    (l.created_at at time zone 'Asia/Seoul')::date as stats_date,
                    count(*) as total_log_count,
                    count(*) filter (where l.status = 'PENDING') as pending_log_count,
                    count(*) filter (where l.status = 'CONFIRMED') as confirmed_log_count,
                    count(*) filter (where l.status = 'REFUNDED') as refunded_log_count,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'INITIAL_FREE_GRANT'
                         and l.free_turn_delta > 0
                        then l.free_turn_delta else 0 end), 0) as free_turns_granted,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_SUCCESS_CONSUME'
                         and l.free_turn_delta < 0
                        then -l.free_turn_delta else 0 end), 0) as free_turns_consumed,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_SUCCESS_CONSUME'
                         and l.paid_turn_delta < 0
                        then -l.paid_turn_delta else 0 end), 0) as paid_turns_consumed,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_FAILURE_REFUND'
                         and l.free_turn_delta > 0
                        then l.free_turn_delta else 0 end), 0) as free_turns_refunded,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_FAILURE_REFUND'
                         and l.paid_turn_delta > 0
                        then l.paid_turn_delta else 0 end), 0) as paid_turns_refunded,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'CRYSTAL_CHARGE'
                         and l.paid_turn_delta > 0
                        then l.paid_turn_delta else 0 end), 0) as paid_turns_charged,
                    coalesce(sum(l.free_turn_delta), 0) as net_free_turn_delta,
                    coalesce(sum(l.paid_turn_delta), 0) as net_paid_turn_delta
                from ask_chat_wallet_logs l
                where l.created_at >= :startInclusive
                  and l.created_at < :endExclusive
                group by 1
                order by 1
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatDailyWalletStatsDto(
                        toLocalDate(row[0]),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toLong(row[4]),
                        toLong(row[5]),
                        toLong(row[6]),
                        toLong(row[7]),
                        toLong(row[8]),
                        toLong(row[9]),
                        toLong(row[10]),
                        toLong(row[11]),
                        toLong(row[12])
                ))
                .toList();
    }

    public AskChatWalletSummaryDto findWalletSummary(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        Object[] row = (Object[]) em.createNativeQuery("""
                select
                    count(*) as total_log_count,
                    count(*) filter (where l.status = 'PENDING') as pending_log_count,
                    count(*) filter (where l.status = 'CONFIRMED') as confirmed_log_count,
                    count(*) filter (where l.status = 'REFUNDED') as refunded_log_count,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'INITIAL_FREE_GRANT'
                         and l.free_turn_delta > 0
                        then l.free_turn_delta else 0 end), 0) as free_turns_granted,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_SUCCESS_CONSUME'
                         and l.free_turn_delta < 0
                        then -l.free_turn_delta else 0 end), 0) as free_turns_consumed,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_SUCCESS_CONSUME'
                         and l.paid_turn_delta < 0
                        then -l.paid_turn_delta else 0 end), 0) as paid_turns_consumed,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_FAILURE_REFUND'
                         and l.free_turn_delta > 0
                        then l.free_turn_delta else 0 end), 0) as free_turns_refunded,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'ANSWER_FAILURE_REFUND'
                         and l.paid_turn_delta > 0
                        then l.paid_turn_delta else 0 end), 0) as paid_turns_refunded,
                    coalesce(sum(case
                        when l.status = 'CONFIRMED'
                         and l.reason = 'CRYSTAL_CHARGE'
                         and l.paid_turn_delta > 0
                        then l.paid_turn_delta else 0 end), 0) as paid_turns_charged,
                    coalesce(sum(l.free_turn_delta), 0) as net_free_turn_delta,
                    coalesce(sum(l.paid_turn_delta), 0) as net_paid_turn_delta
                from ask_chat_wallet_logs l
                where l.created_at >= :startInclusive
                  and l.created_at < :endExclusive
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getSingleResult();

        return new AskChatWalletSummaryDto(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toLong(row[5]),
                toLong(row[6]),
                toLong(row[7]),
                toLong(row[8]),
                toLong(row[9]),
                toLong(row[10]),
                toLong(row[11])
        );
    }

    public List<AskChatDailyRagDocumentStatsDto> findDailyRagDocumentStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    (d.created_at at time zone 'Asia/Seoul')::date as stats_date,
                    count(*) as total_document_count,
                    count(*) filter (where d.embedding_status = 'PENDING') as pending_document_count,
                    count(*) filter (where d.embedding_status = 'COMPLETED') as completed_document_count,
                    count(*) filter (where d.embedding_status = 'FAILED') as failed_document_count,
                    count(*) filter (where d.embedding_status = 'DEAD_LETTER') as dead_letter_document_count,
                    avg(d.retry_count) filter (
                        where d.embedding_status in ('FAILED', 'DEAD_LETTER')
                    ) as average_failed_retry_count
                from ask_chat_rag_documents d
                where d.created_at >= :startInclusive
                  and d.created_at < :endExclusive
                group by 1
                order by 1
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatDailyRagDocumentStatsDto(
                        toLocalDate(row[0]),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toLong(row[4]),
                        toLong(row[5]),
                        toDouble(row[6])
                ))
                .toList();
    }

    public List<AskChatDailyRagReferenceStatsDto> findDailyRagReferenceStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    (r.created_at at time zone 'Asia/Seoul')::date as stats_date,
                    count(*) as reference_count,
                    count(distinct r.rag_document_id) as unique_referenced_document_count
                from ask_chat_message_references r
                where r.created_at >= :startInclusive
                  and r.created_at < :endExclusive
                group by 1
                order by 1
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatDailyRagReferenceStatsDto(
                        toLocalDate(row[0]),
                        toLong(row[1]),
                        toLong(row[2])
                ))
                .toList();
    }

    public AskChatRagSummaryDto findRagSummary(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        Object[] row = (Object[]) em.createNativeQuery("""
                with document_stats as (
                    select
                        count(*) as total_document_count,
                        count(*) filter (where d.embedding_status = 'PENDING') as pending_document_count,
                        count(*) filter (where d.embedding_status = 'COMPLETED') as completed_document_count,
                        count(*) filter (where d.embedding_status = 'FAILED') as failed_document_count,
                        count(*) filter (where d.embedding_status = 'DEAD_LETTER') as dead_letter_document_count,
                        avg(d.retry_count) filter (
                            where d.embedding_status in ('FAILED', 'DEAD_LETTER')
                        ) as average_failed_retry_count
                    from ask_chat_rag_documents d
                    where d.created_at >= :startInclusive
                      and d.created_at < :endExclusive
                ), reference_stats as (
                    select
                        count(*) as total_reference_count,
                        count(distinct r.rag_document_id) as unique_referenced_document_count
                    from ask_chat_message_references r
                    where r.created_at >= :startInclusive
                      and r.created_at < :endExclusive
                )
                select
                    d.total_document_count,
                    d.pending_document_count,
                    d.completed_document_count,
                    d.failed_document_count,
                    d.dead_letter_document_count,
                    d.average_failed_retry_count,
                    r.total_reference_count,
                    r.unique_referenced_document_count
                from document_stats d
                cross join reference_stats r
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getSingleResult();

        return new AskChatRagSummaryDto(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toDouble(row[5]),
                toLong(row[6]),
                toLong(row[7])
        );
    }

    public List<AskChatRagSourceStatsDto> findRagSourceStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    d.source_type,
                    count(*) as total_document_count,
                    count(*) filter (where d.embedding_status = 'PENDING') as pending_document_count,
                    count(*) filter (where d.embedding_status = 'COMPLETED') as completed_document_count,
                    count(*) filter (where d.embedding_status = 'FAILED') as failed_document_count,
                    count(*) filter (where d.embedding_status = 'DEAD_LETTER') as dead_letter_document_count
                from ask_chat_rag_documents d
                where d.created_at >= :startInclusive
                  and d.created_at < :endExclusive
                group by d.source_type
                order by total_document_count desc, d.source_type asc
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatRagSourceStatsDto(
                        String.valueOf(row[0]),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toLong(row[4]),
                        toLong(row[5])
                ))
                .toList();
    }

    public List<AskChatRagErrorStatsDto> findRagErrorStats(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        List<Object[]> rows = em.createNativeQuery("""
                select
                    coalesce(nullif(trim(d.error_code), ''), 'UNKNOWN') as error_code,
                    count(*) as error_count
                from ask_chat_rag_documents d
                where d.created_at >= :startInclusive
                  and d.created_at < :endExclusive
                  and d.embedding_status in ('FAILED', 'DEAD_LETTER')
                group by 1
                order by error_count desc, error_code asc
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(row -> new AskChatRagErrorStatsDto(String.valueOf(row[0]), toLong(row[1])))
                .toList();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        String text = String.valueOf(value);
        return LocalDate.parse(text.substring(0, 10));
    }

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }
}
