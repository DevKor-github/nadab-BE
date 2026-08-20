package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportDateInterestCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestCountDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TypeStatsRepository {

    private final EntityManager em;

    public long countInProgressTypeReportsNow() {
        return em.createQuery("""
                select count(tr.id)
                from TypeReport tr
                where tr.status = com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus.IN_PROGRESS
                  and tr.deletedAt is null
                """, Long.class)
                .getSingleResult();
    }

    public List<TypeReportInterestCountDto> countCompletedTypeReportsByInterest() {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestCountDto(
                    tr.interestCode,
                    count(tr.id)
                )
                from TypeReport tr
                where tr.status = com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus.COMPLETED
                group by tr.interestCode
                order by tr.interestCode
                """, TypeReportInterestCountDto.class)
                .getResultList();
    }

    public List<TypeReportDateInterestCountDto> countCompletedTypeReportsByDateAndInterest(
            LocalDate startDate,
            LocalDate endDateInclusive
    ) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportDateInterestCountDto(
                    tr.date,
                    tr.interestCode,
                    count(tr.id)
                )
                from TypeReport tr
                where tr.status = com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus.COMPLETED
                  and tr.date between :startDate and :endDate
                group by tr.date, tr.interestCode
                order by tr.date, tr.interestCode
                """, TypeReportDateInterestCountDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDateInclusive)
                .getResultList();
    }
}
