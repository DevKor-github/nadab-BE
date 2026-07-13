package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagSearchResultDto;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

@Repository
@RequiredArgsConstructor
public class AskChatRagVectorRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public int updateEmbedding(Long documentId, List<Double> embedding) {
        String sql = """
                UPDATE ask_chat_rag_documents
                   SET embedding = CAST(:embedding AS vector),
                       embedding_status = 'COMPLETED',
                       error_code = NULL,
                       embedded_at = CURRENT_TIMESTAMP,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE id = :documentId
                """;

        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("documentId", documentId)
                .addValue("embedding", formatVector(embedding)));
    }

    public int markEmbeddingFailed(Long documentId, String errorCode) {
        String sql = """
                UPDATE ask_chat_rag_documents
                   SET embedding_status = 'FAILED',
                       error_code = :errorCode,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE id = :documentId
                """;

        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("documentId", documentId)
                .addValue("errorCode", errorCode));
    }

    public List<AskChatRagSearchResultDto> search(
            Long userId,
            InterestCode interestCode,
            List<Double> queryEmbedding,
            int limit
    ) {
        String sql = """
                SELECT id,
                       source_type,
                       source_id,
                       interest_code,
                       content,
                       embedding <=> CAST(:queryEmbedding AS vector) AS distance
                  FROM ask_chat_rag_documents
                 WHERE user_id = :userId
                   AND embedding_status = 'COMPLETED'
                   AND embedding IS NOT NULL
                   AND (:interestCode IS NULL OR interest_code = :interestCode)
                 ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
                 LIMIT :limit
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("interestCode", interestCode == null ? null : interestCode.name())
                .addValue("queryEmbedding", formatVector(queryEmbedding))
                .addValue("limit", limit), this::mapSearchResult);
    }

    private AskChatRagSearchResultDto mapSearchResult(ResultSet rs, int rowNum) throws SQLException {
        String interestCode = rs.getString("interest_code");
        return new AskChatRagSearchResultDto(
                rs.getLong("id"),
                AskChatRagDocumentSourceType.valueOf(rs.getString("source_type")),
                rs.getLong("source_id"),
                interestCode == null ? null : InterestCode.valueOf(interestCode),
                rs.getString("content"),
                rs.getDouble("distance")
        );
    }

    private String formatVector(List<Double> embedding) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Double value : embedding) {
            joiner.add(Double.toString(value));
        }
        return joiner.toString();
    }
}
