package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnswerEntryQueryRepository extends Repository<AnswerEntry, Long> {

    /**
     * 첫 페이지 조회 (cursor 없음)
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto(
            ae.id, ae.question.interest.code, e.code, ae.question.questionText, ae.content, ae.date
        )
        from AnswerEntry ae
        left join DailyReport dr on dr.answerEntry = ae and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        left join dr.emotion e
        where ae.user.id = :userId
          and (:keyword is null or ae.question.questionText like :keyword escape '\\' or ae.content like :keyword escape '\\')
          and (:emotionCode is null or e.code = :emotionCode)
        order by ae.date desc
        """)
    List<SearchAnswerEntryDto> searchAnswerEntriesFirstPage(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("emotionCode") EmotionCode emotionCode,
            Pageable pageable
    );

    /**
     * 다음 페이지 조회 (cursor 있음)
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto(
            ae.id, ae.question.interest.code, e.code, ae.question.questionText, ae.content, ae.date
        )
        from AnswerEntry ae
        left join DailyReport dr on dr.answerEntry = ae and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        left join dr.emotion e
        where ae.user.id = :userId
          and (:keyword is null or ae.question.questionText like :keyword escape '\\' or ae.content like :keyword escape '\\')
          and (:emotionCode is null or e.code = :emotionCode)
          and ae.date < :cursorDate
        order by ae.date desc
        """)
    List<SearchAnswerEntryDto> searchAnswerEntriesWithCursor(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("emotionCode") EmotionCode emotionCode,
            @Param("cursorDate") LocalDate cursorDate,
            Pageable pageable
    );
}