package com.devkor.ifive.nadab.domain.typereport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TypeDailyEntryQueryRepository extends Repository<DailyReport, Long> {

    @Query("""
    select new com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto(
        dr.date,
        dq.questionText,
        ae.content,
        dr.content,
        coalesce(em.name, com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName.기타)
    )
    from DailyReport dr
    join dr.answerEntry ae
    join ae.question dq
    join dq.interest i
    left join dr.emotion em
    where ae.user.id = :userId
      and i.code = :interestCode
      and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
    order by dr.date desc, dr.id desc
""")
    List<DailyEntryDto> findRecentDailyEntriesByInterest(
            @Param("userId") Long userId,
            @Param("interestCode") InterestCode interestCode,
            Pageable pageable
    );
}