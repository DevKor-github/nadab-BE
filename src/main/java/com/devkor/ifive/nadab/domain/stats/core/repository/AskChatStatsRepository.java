package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
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
