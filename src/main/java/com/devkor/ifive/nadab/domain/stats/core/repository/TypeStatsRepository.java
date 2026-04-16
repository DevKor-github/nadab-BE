package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

    public List<LabelCountDto> countCompletedTypeReportsByInterest() {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto(
                    cast(i.code as string),
                    count(tr.id)
                )
                from Interest i
                left join TypeReport tr on tr.interestCode = i.code
                                       and tr.status = com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus.COMPLETED
                                       and tr.deletedAt is null
                group by i.code
                order by i.code
                """, LabelCountDto.class)
                .getResultList();
    }
}
