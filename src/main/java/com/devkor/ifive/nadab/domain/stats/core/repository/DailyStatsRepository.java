package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
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
public class DailyStatsRepository {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final EntityManager em;

    /**
     * 최근 7일 가입자 수 (registeredAt 기준)
     */
    public List<Object[]> findSignupCountsLast7Days(LocalDate startDate, LocalDate endDateInclusive) {
        OffsetDateTime start = startDate.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime endExclusive = endDateInclusive.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();

        // JPQL에서 "날짜 단위"로 묶기 위해 Postgres date() 함수 사용
        // function('date', u.registeredAt) -> java.sql.Date 로 내려오는 경우가 많아서 Object[]로 받는게 안전
        return em.createQuery("""
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
    }

    /**
     * 최근 7일 할당된 질문 수 (user_daily_questions)
     */
    public List<DateCountDto> findAssignedQuestionCountsLast7Days(LocalDate startDate, LocalDate endDateInclusive) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.DateCountDto(udq.date, count(udq.id))
                from UserDailyQuestion udq
                where udq.date between :startDate and :endDate
                group by udq.date
                order by udq.date
                """, DateCountDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDateInclusive)
                .getResultList();
    }

    /**
     * 최근 7일 COMPLETED daily_reports 수
     */
    public List<DateCountDto> findCompletedDailyReportCountsLast7Days(LocalDate startDate, LocalDate endDateInclusive) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.DateCountDto(dr.date, count(dr.id))
                from DailyReport dr
                where dr.date between :startDate and :endDate
                  and dr.status = 'COMPLETED'
                group by dr.date
                order by dr.date
                """, DateCountDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDateInclusive)
                .getResultList();
    }

    /**
     * 현재 공유 중(isShared=true) daily_reports 개수
     * - 보통 COMPLETED만 공유 의미가 있으니 status도 거는 걸 추천
     */
    public long countSharedDailyReportsNow() {
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        return em.createQuery("""
            select count(dr.id)
            from DailyReport dr
            where dr.date = :today
              and dr.isShared = true
              and dr.status = 'COMPLETED'
            """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    // ---------- 유틸: Object[] -> DateCountDto 변환에 사용할 때 ----------
    public static DateCountDto toDateCountDto(Object[] row) {
        // row[0] = java.sql.Date (대개), row[1] = Long
        LocalDate date = ((Date) row[0]).toLocalDate();
        long count = (Long) row[1];
        return new DateCountDto(date, count);
    }
}
