package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AnswerDetailDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.MonthlyCalendarDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    /**
     * 월별 캘린더 데이터 조회
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.MonthlyCalendarDto(
            ae.date, e.code
        )
        from AnswerEntry ae
        left join DailyReport dr on dr.answerEntry = ae and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        left join dr.emotion e
        where ae.user.id = :userId
          and ae.date >= :startDate
          and ae.date <= :endDate
        order by ae.date asc
        """)
    List<MonthlyCalendarDto> findCalendarEntriesInMonth(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 최근 N개 답변 조회 (날짜 내림차순)
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto(
            ae.id, ae.question.interest.code, e.code, ae.question.questionText, ae.content, ae.date
        )
        from AnswerEntry ae
        left join DailyReport dr on dr.answerEntry = ae and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        left join dr.emotion e
        where ae.user.id = :userId
        order by ae.date desc
        """)
    List<SearchAnswerEntryDto> findRecentAnswers(
            @Param("userId") Long userId,
            Pageable pageable
    );


    /**
     * 답변 ID로 상세 조회
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.AnswerDetailDto(
            ae.question.questionText, ae.question.interest.code, ae.date, ae.content, dr.content, e.code
        )
        from AnswerEntry ae
        left join DailyReport dr on dr.answerEntry = ae and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        left join dr.emotion e
        where ae.id = :answerId
          and ae.user.id = :userId
        """)
    Optional<AnswerDetailDto> findDetailByAnswerId(
            @Param("userId") Long userId,
            @Param("answerId") Long answerId
    );

    /**
     * 날짜로 상세 조회
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.AnswerDetailDto(
            ae.question.questionText, ae.question.interest.code, ae.date, ae.content, dr.content, e.code
        )
        from AnswerEntry ae
        left join DailyReport dr on dr.answerEntry = ae and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        left join dr.emotion e
        where ae.user.id = :userId
          and ae.date = :date
        """)
    Optional<AnswerDetailDto> findDetailByUserAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    /**
     * 사용자의 모든 답변 날짜 조회
     */
    @Query("""
        SELECT ae.date
        FROM AnswerEntry ae
        WHERE ae.user.id = :userId
        """)
    List<LocalDate> findAllAnswerDatesByUserId(@Param("userId") Long userId);
}