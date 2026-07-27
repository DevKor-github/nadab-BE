package com.devkor.ifive.nadab.domain.pdfexport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * PDF 내보내기 기간 데이터 조회(읽기 전용, 크로스도메인).
 * 답변은 date BETWEEN, 리포트는 overlap(기간과 겹치면 포함)·COMPLETED만.
 */
public interface PdfExportQueryRepository extends Repository<AnswerEntry, Long> {

    /**
     * 기간 내 답변(날짜 오름차순). 질문 INNER, 관심사 LEFT, 감정 LEFT.
     * 감정 = 같은 답변·같은 날짜의 COMPLETED 일간 리포트(유니크라 팬아웃 없음).
     */
    @Query("""
        SELECT new com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto(
                   a.date, a.content, a.imageKey, q.questionText, i.code, e.code)
          FROM AnswerEntry a
          JOIN a.question q
          LEFT JOIN q.interest i
          LEFT JOIN DailyReport dr
                 ON dr.answerEntry = a
                AND dr.date = a.date
                AND dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
          LEFT JOIN dr.emotion e
         WHERE a.user.id = :userId
           AND a.date BETWEEN :startDate AND :endDate
         ORDER BY a.date ASC
    """)
    List<PdfAnswerRowDto> findAnswersInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /** overlap = weekStartDate <= endDate AND weekEndDate >= startDate. */
    @Query("""
        SELECT w FROM WeeklyReport w
         WHERE w.user.id = :userId
           AND w.status = com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus.COMPLETED
           AND w.weekStartDate <= :endDate
           AND w.weekEndDate >= :startDate
         ORDER BY w.weekStartDate ASC
    """)
    List<WeeklyReport> findWeeklyReportsInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /** 월간 V2. overlap = monthStartDate <= endDate AND monthEndDate >= startDate. */
    @Query("""
        SELECT m FROM MonthlyReportV2 m
         WHERE m.user.id = :userId
           AND m.status = com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus.COMPLETED
           AND m.monthStartDate <= :endDate
           AND m.monthEndDate >= :startDate
         ORDER BY m.monthStartDate ASC
    """)
    List<MonthlyReportV2> findMonthlyReportsV2InPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /** 월간 V1(레거시). overlap = monthStartDate <= endDate AND monthEndDate >= startDate. */
    @Query("""
        SELECT m FROM MonthlyReport m
         WHERE m.user.id = :userId
           AND m.status = com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus.COMPLETED
           AND m.monthStartDate <= :endDate
           AND m.monthEndDate >= :startDate
         ORDER BY m.monthStartDate ASC
    """)
    List<MonthlyReport> findMonthlyReportsInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /** 차감 전 데이터 존재 검사용 count. */

    @Query("""
        SELECT COUNT(a) FROM AnswerEntry a
         WHERE a.user.id = :userId
           AND a.date BETWEEN :startDate AND :endDate
    """)
    long countAnswersInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(w) FROM WeeklyReport w
         WHERE w.user.id = :userId
           AND w.status = com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus.COMPLETED
           AND w.weekStartDate <= :endDate
           AND w.weekEndDate >= :startDate
    """)
    long countWeeklyReportsInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(m) FROM MonthlyReportV2 m
         WHERE m.user.id = :userId
           AND m.status = com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus.COMPLETED
           AND m.monthStartDate <= :endDate
           AND m.monthEndDate >= :startDate
    """)
    long countMonthlyReportsV2InPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(m) FROM MonthlyReport m
         WHERE m.user.id = :userId
           AND m.status = com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus.COMPLETED
           AND m.monthStartDate <= :endDate
           AND m.monthEndDate >= :startDate
    """)
    long countMonthlyReportsInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}