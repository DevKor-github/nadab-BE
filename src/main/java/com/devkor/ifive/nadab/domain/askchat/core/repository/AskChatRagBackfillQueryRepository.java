package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillTargetDto;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AskChatRagBackfillQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<AskChatRagBackfillTargetDto> findCompletedDailyAnswerTargets(int embeddingVersion, int limit) {
        String sql = """
                SELECT ae.id AS answer_entry_id,
                       dr.id AS report_id,
                       i.code AS interest_code
                  FROM daily_reports dr
                  JOIN answer_entries ae ON ae.id = dr.answer_entry_id
                  JOIN daily_questions dq ON dq.id = ae.question_id
                  LEFT JOIN interests i ON i.id = dq.interest_id
                 WHERE dr.status = 'COMPLETED'
                   AND NOT EXISTS (
                       SELECT 1
                         FROM ask_chat_rag_documents d
                        WHERE d.source_type = 'ANSWER_ENTRY'
                          AND d.source_id = ae.id
                          AND d.embedding_version = :embeddingVersion
                   )
                 ORDER BY dr.created_at ASC, dr.id ASC
                 LIMIT :limit
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("embeddingVersion", embeddingVersion)
                .addValue("limit", limit), this::mapTarget);
    }

    private AskChatRagBackfillTargetDto mapTarget(ResultSet rs, int rowNum) throws SQLException {
        String interestCode = rs.getString("interest_code");
        return new AskChatRagBackfillTargetDto(
                rs.getLong("answer_entry_id"),
                rs.getLong("report_id"),
                interestCode == null ? null : InterestCode.valueOf(interestCode)
        );
    }
}
