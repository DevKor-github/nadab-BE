package com.devkor.ifive.nadab.domain.typereport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TypeDailyEntryQueryRepository extends Repository<DailyReport, Long> {

    @Query(value = """
        SELECT
            dr.date AS date,
            dq.question_text AS question,
            ae.content AS answer,
            dr.content AS dailyReport,
            e.name AS emotion
        FROM daily_reports dr
        JOIN answer_entries ae ON ae.id = dr.answer_entry_id
        JOIN daily_questions dq ON dq.id = ae.question_id
        LEFT JOIN emotions e ON e.id = dr.emotion_id
        JOIN interests i ON i.id = dq.interest_id
        WHERE ae.user_id = :userId
          AND i.code = :interestCode
          AND dr.status = 'COMPLETED'
        ORDER BY dr.date DESC, dr.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<DailyEntryDto> findRecentDailyEntriesByInterest(
            @Param("userId") Long userId,
            @Param("interestCode") String interestCode,
            @Param("limit") int limit
    );
}