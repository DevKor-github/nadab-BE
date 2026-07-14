package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillTargetDto;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        AskChatRagBackfillQueryRepository.class,
        AskChatRagVectorRepository.class
})
class AskChatRagRepositoryIntegrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    private AskChatRagBackfillQueryRepository backfillQueryRepository;

    @Autowired
    private AskChatRagVectorRepository vectorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findCompletedDailyAnswerTargets_excludes_already_indexed_sources_and_limits_batch() {
        Long userId = insertUser("backfill-target");
        Long relationshipQuestionId = insertDailyQuestion(InterestCode.RELATIONSHIP, "relationship question");
        Long routineQuestionId = insertDailyQuestion(InterestCode.ROUTINE, "routine question");
        Long firstAnswerId = insertAnswerEntry(userId, relationshipQuestionId, "first answer", LocalDate.of(2026, 7, 10));
        Long secondAnswerId = insertAnswerEntry(userId, routineQuestionId, "second answer", LocalDate.of(2026, 7, 11));
        Long thirdAnswerId = insertAnswerEntry(userId, relationshipQuestionId, "third answer", LocalDate.of(2026, 7, 12));
        insertDailyReport(firstAnswerId, "first report", "COMPLETED", LocalDate.of(2026, 7, 10));
        Long secondReportId = insertDailyReport(secondAnswerId, "second report", "COMPLETED", LocalDate.of(2026, 7, 11));
        Long thirdReportId = insertDailyReport(thirdAnswerId, "third report", "COMPLETED", LocalDate.of(2026, 7, 12));
        insertRagDocument(userId, "ANSWER_ENTRY", firstAnswerId, InterestCode.RELATIONSHIP, 1, "COMPLETED");

        List<AskChatRagBackfillTargetDto> targets =
                backfillQueryRepository.findCompletedDailyAnswerTargets(1, 1);

        assertThat(targets).singleElement()
                .satisfies(target -> {
                    assertThat(target.answerEntryId()).isEqualTo(secondAnswerId);
                    assertThat(target.reportId()).isEqualTo(secondReportId);
                    assertThat(target.interestCode()).isEqualTo(InterestCode.ROUTINE);
                });

        List<AskChatRagBackfillTargetDto> allTargets =
                backfillQueryRepository.findCompletedDailyAnswerTargets(1, 10);

        assertThat(allTargets)
                .extracting(AskChatRagBackfillTargetDto::answerEntryId)
                .containsExactly(secondAnswerId, thirdAnswerId);
        assertThat(allTargets)
                .extracting(AskChatRagBackfillTargetDto::reportId)
                .containsExactly(secondReportId, thirdReportId);
    }

    @Test
    void updateEmbedding_and_markEmbeddingFailed_update_pgvector_document_status() {
        Long userId = insertUser("vector-update");
        Long completedDocumentId = insertRagDocument(
                userId,
                "ANSWER_ENTRY",
                1000L,
                InterestCode.RELATIONSHIP,
                1,
                "PENDING"
        );
        Long failedDocumentId = insertRagDocument(
                userId,
                "ANSWER_ENTRY",
                1001L,
                InterestCode.ROUTINE,
                1,
                "PENDING"
        );

        int completed = vectorRepository.updateEmbedding(completedDocumentId, embedding(1536));
        int failed = vectorRepository.markEmbeddingFailed(failedDocumentId, "IllegalStateException", 3);

        assertThat(completed).isEqualTo(1);
        assertThat(failed).isEqualTo(1);
        assertThat(findDocumentStatus(completedDocumentId)).isEqualTo("COMPLETED");
        assertThat(findDocumentStatus(failedDocumentId)).isEqualTo("FAILED");
        assertThat(findRetryCount(failedDocumentId)).isEqualTo(1);
        assertThat(hasEmbedding(completedDocumentId)).isTrue();
    }

    private Long insertUser(String prefix) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO users (email, password_hash, nickname, signup_status)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """, Long.class, prefix + "@test.com", "hashed", prefix, "COMPLETED");
    }

    private Long insertDailyQuestion(InterestCode interestCode, String questionText) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO daily_questions (interest_id, question_text, question_level)
                SELECT id, ?, 1
                  FROM interests
                 WHERE code = ?
                RETURNING id
                """, Long.class, questionText, interestCode.name());
    }

    private Long insertAnswerEntry(Long userId, Long questionId, String content, LocalDate date) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO answer_entries (user_id, question_id, content, date)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """, Long.class, userId, questionId, content, date);
    }

    private Long insertDailyReport(Long answerEntryId, String content, String status, LocalDate date) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO daily_reports (answer_entry_id, content, status, date, created_at)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """, Long.class, answerEntryId, content, status, date, date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset()));
    }

    private Long insertRagDocument(
            Long userId,
            String sourceType,
            Long sourceId,
            InterestCode interestCode,
            int embeddingVersion,
            String embeddingStatus
    ) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO ask_chat_rag_documents (
                    user_id,
                    source_type,
                    source_id,
                    interest_code,
                    content,
                    metadata,
                    embedding_model,
                    embedding_version,
                    embedding_status
                )
                VALUES (?, ?, ?, ?, ?, '{}'::jsonb, ?, ?, ?)
                RETURNING id
                """, Long.class,
                userId,
                sourceType,
                sourceId,
                interestCode.name(),
                "content",
                "text-embedding-3-small",
                embeddingVersion,
                embeddingStatus);
    }

    private String findDocumentStatus(Long documentId) {
        return jdbcTemplate.queryForObject(
                "SELECT embedding_status FROM ask_chat_rag_documents WHERE id = ?",
                String.class,
                documentId
        );
    }

    private Integer findRetryCount(Long documentId) {
        return jdbcTemplate.queryForObject(
                "SELECT retry_count FROM ask_chat_rag_documents WHERE id = ?",
                Integer.class,
                documentId
        );
    }

    private boolean hasEmbedding(Long documentId) {
        Boolean result = jdbcTemplate.queryForObject(
                "SELECT embedding IS NOT NULL FROM ask_chat_rag_documents WHERE id = ?",
                Boolean.class,
                documentId
        );
        return Boolean.TRUE.equals(result);
    }

    private List<Double> embedding(int dimensions) {
        return java.util.stream.IntStream.range(0, dimensions)
                .mapToDouble(index -> index == 0 ? 1.0 : 0.0)
                .boxed()
                .toList();
    }
}
