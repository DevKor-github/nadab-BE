package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MonthlyStatsRepository {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final EntityManager em;

    public List<DateCountDto> findSignupCountsByDateBetween(LocalDate startDate, LocalDate endDateInclusive) {
        OffsetDateTime start = startDate.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime endExclusive = endDateInclusive.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();

        List<Object[]> rows = em.createQuery("""
            select function('date', u.registeredAt), count(u.id)
            from User u
            where u.deletedAt is null
              and u.signupStatus = com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType.COMPLETED
              and u.registeredAt is not null
              and u.registeredAt >= :start
              and u.registeredAt < :endExclusive
            group by function('date', u.registeredAt)
            order by function('date', u.registeredAt)
            """, Object[].class)
                .setParameter("start", start)
                .setParameter("endExclusive", endExclusive)
                .getResultList();

        return rows.stream()
                .map(MonthlyStatsRepository::toDateCountDto)
                .toList();
    }

    public List<DateCountDto> findAssignedQuestionCountsByDateBetween(LocalDate startDate, LocalDate endDateInclusive) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto(udq.date, count(udq.id))
                from UserDailyQuestion udq
                where udq.date between :startDate and :endDate
                group by udq.date
                order by udq.date
                """, DateCountDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDateInclusive)
                .getResultList();
    }

    public List<DateCountDto> findCompletedMonthlyReportCountsByDateBetween(LocalDate startDate, LocalDate endDateInclusive) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto(mr.date, count(mr.id))
                from MonthlyReport mr
                where mr.date between :startDate and :endDate
                  and mr.status = com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus.COMPLETED
                group by mr.date
                order by mr.date
                """, DateCountDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDateInclusive)
                .getResultList();
    }

    public long countInProgressMonthlyReportsNow() {
        return em.createQuery("""
            select count(mr.id)
            from MonthlyReport mr
            where mr.status = com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus.IN_PROGRESS
            """, Long.class)
                .getSingleResult();
    }

    private static DateCountDto toDateCountDto(Object[] row) {
        LocalDate date = ((Date) row[0]).toLocalDate();
        long count = (Long) row[1];
        return new DateCountDto(date, count);
    }
}
