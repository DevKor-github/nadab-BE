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
public class WeeklyStatsRepository {

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
                .map(WeeklyStatsRepository::toDateCountDto)
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

    public List<DateCountDto> findCompletedWeeklyReportCountsByDateBetween(
            LocalDate startDate,
            LocalDate endDateInclusive
    ) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto(wr.date, count(wr.id))
                from WeeklyReport wr
                where wr.date between :startDate and :endDate
                  and wr.status = com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus.COMPLETED
                group by wr.date
                order by wr.date
                """, DateCountDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDateInclusive)
                .getResultList();
    }

    public long countInProgressWeeklyReportsNow() {
        return em.createQuery("""
            select count(wr.id)
            from WeeklyReport wr
            where wr.status = com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus.IN_PROGRESS
            """, Long.class)
                .getSingleResult();
    }

    private static DateCountDto toDateCountDto(Object[] row) {
        LocalDate date = ((Date) row[0]).toLocalDate();
        long count = (Long) row[1];
        return new DateCountDto(date, count);
    }
}
