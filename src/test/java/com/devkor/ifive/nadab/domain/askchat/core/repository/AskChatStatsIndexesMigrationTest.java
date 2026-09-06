package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatStatsIndexesMigrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void stats_period_indexes_are_created_for_global_aggregations() {
        assertThat(indexExists("ask_chat_sessions", "idx_ask_chat_sessions_stats_created_id")).isTrue();
        assertThat(indexExists("ask_chat_messages", "idx_ask_chat_messages_stats_created_id")).isTrue();
        assertThat(indexExists(
                "ask_chat_message_references",
                "idx_ask_chat_message_references_stats_created_id"
        )).isTrue();
        assertThat(indexExists(
                "ask_chat_rag_documents",
                "idx_ask_chat_rag_documents_stats_created_id"
        )).isTrue();
    }

    private boolean indexExists(String tableName, String indexName) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM pg_indexes
                    WHERE schemaname = current_schema()
                      AND tablename = ?
                      AND indexname = ?
                )
                """, Boolean.class, tableName, indexName);
        return Boolean.TRUE.equals(exists);
    }
}
