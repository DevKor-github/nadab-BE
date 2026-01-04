package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnswerEntryQueryRepository extends Repository<AnswerEntry, Long> {

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
          and (:cursorDate is null or ae.date < :cursorDate)
        order by ae.date desc
        limit :size
        """)
    List<SearchAnswerEntryDto> searchAnswerEntries(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("emotionCode") EmotionCode emotionCode,
            @Param("cursorDate") LocalDate cursorDate,
            @Param("size") int size
    );
}