package com.devkor.ifive.nadab.domain.monthlyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MonthlyQueryRepository extends JpaRepository<AnswerEntry, Long> {

    /**
     * 월간 리포트 작성을 위한 입력 데이터 조회
     */
    @Query("""
    select new com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto(
        ae.date,
        dq.questionText,
        ae.content,
        dr.content,
        em.name
    )
    from AnswerEntry ae
    join ae.question dq
    left join DailyReport dr
           on dr.answerEntry = ae
          and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
    left join dr.emotion em
    where ae.user.id = :userId
      and ae.date between :monthStart and :monthEnd
    order by ae.date asc
""")
    List<DailyEntryDto> findMonthlyInputs(
            @Param("userId") Long userId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );
}
