package com.devkor.ifive.nadab.domain.weeklyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyQueryRepository extends Repository<AnswerEntry, Long> {

    /**
     * 주간 리포트 작성을 위한 입력 데이터 조회
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
      and ae.date between :weekStart and :weekEnd
    order by ae.date asc
""")

    List<DailyEntryDto> findWeeklyInputs(
            @Param("userId") Long userId,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );
}
