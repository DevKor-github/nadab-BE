package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStat;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PeakStatsRepository {

    private static final String FIND_ALL_PEAKS_QUERY = """
            with signup_daily as (
                select cast(u.registered_at at time zone 'Asia/Seoul' as date) as period_start,
                       count(*) as value
                from users u
                where u.signup_status = 'COMPLETED'
                  and u.registered_at is not null
                  and cast(u.registered_at at time zone 'Asia/Seoul' as date)
                      <= cast(current_timestamp at time zone 'Asia/Seoul' as date)
                group by cast(u.registered_at at time zone 'Asia/Seoul' as date)
            ),
            assigned_daily as (
                select udq.date as period_start,
                       count(*) as value
                from user_daily_questions udq
                where udq.date <= cast(current_timestamp at time zone 'Asia/Seoul' as date)
                group by udq.date
            ),
            completed_daily_reports as (
                select dr.date as report_date,
                       ae.user_id
                from daily_reports dr
                join answer_entries ae on ae.id = dr.answer_entry_id
                where dr.status = 'COMPLETED'
                  and dr.date <= cast(current_timestamp at time zone 'Asia/Seoul' as date)
            ),
            completed_monthly_reports as (
                select cast(date_trunc('month', mr.date) as date) as period_start,
                       count(*) as value
                from monthly_reports mr
                where mr.status = 'COMPLETED'
                  and mr.date <= cast(current_timestamp at time zone 'Asia/Seoul' as date)
                group by cast(date_trunc('month', mr.date) as date)
                union all
                select cast(date_trunc('month', mr.date) as date) as period_start,
                       count(*) as value
                from monthly_reports_v2 mr
                where mr.status = 'COMPLETED'
                  and mr.date <= cast(current_timestamp at time zone 'Asia/Seoul' as date)
                group by cast(date_trunc('month', mr.date) as date)
            ),
            metric_counts as (
                select 'DAILY_SIGNUP' as metric, period_start, value
                from signup_daily
                union all
                select 'DAILY_ASSIGNED_QUESTION', period_start, value
                from assigned_daily
                union all
                select 'DAU', report_date, count(distinct user_id)
                from completed_daily_reports
                group by report_date
                union all
                select 'WEEKLY_SIGNUP', cast(date_trunc('week', period_start) as date), sum(value)
                from signup_daily
                group by cast(date_trunc('week', period_start) as date)
                union all
                select 'WEEKLY_ASSIGNED_QUESTION', cast(date_trunc('week', period_start) as date), sum(value)
                from assigned_daily
                group by cast(date_trunc('week', period_start) as date)
                union all
                select 'WEEKLY_DAILY_REPORT', cast(date_trunc('week', report_date) as date), count(*)
                from completed_daily_reports
                group by cast(date_trunc('week', report_date) as date)
                union all
                select 'WEEKLY_REPORT', cast(date_trunc('week', wr.date) as date), count(*)
                from weekly_reports wr
                where wr.status = 'COMPLETED'
                  and wr.date <= cast(current_timestamp at time zone 'Asia/Seoul' as date)
                group by cast(date_trunc('week', wr.date) as date)
                union all
                select 'WAU', cast(date_trunc('week', report_date) as date), count(distinct user_id)
                from completed_daily_reports
                group by cast(date_trunc('week', report_date) as date)
                union all
                select 'MONTHLY_SIGNUP', cast(date_trunc('month', period_start) as date), sum(value)
                from signup_daily
                group by cast(date_trunc('month', period_start) as date)
                union all
                select 'MONTHLY_ASSIGNED_QUESTION', cast(date_trunc('month', period_start) as date), sum(value)
                from assigned_daily
                group by cast(date_trunc('month', period_start) as date)
                union all
                select 'MONTHLY_DAILY_REPORT', cast(date_trunc('month', report_date) as date), count(*)
                from completed_daily_reports
                group by cast(date_trunc('month', report_date) as date)
                union all
                select 'MONTHLY_REPORT', period_start, sum(value)
                from completed_monthly_reports
                group by period_start
                union all
                select 'MAU', cast(date_trunc('month', report_date) as date), count(distinct user_id)
                from completed_daily_reports
                group by cast(date_trunc('month', report_date) as date)
            ),
            ranked as (
                select metric,
                       period_start,
                       value,
                       row_number() over (
                           partition by metric
                           order by value desc, period_start desc
                       ) as ranking
                from metric_counts
                where value > 0
            )
            select metric, period_start, value
            from ranked
            where ranking = 1
            order by metric
            """;

    private final EntityManager em;

    public Map<PeakMetric, PeakStat> findAllPeakStats() {
        List<?> rows = em.createNativeQuery(FIND_ALL_PEAKS_QUERY).getResultList();
        EnumMap<PeakMetric, PeakStat> peaks = new EnumMap<>(PeakMetric.class);

        for (Object rawRow : rows) {
            Object[] row = (Object[]) rawRow;
            PeakMetric metric = PeakMetric.valueOf(row[0].toString());
            peaks.put(metric, new PeakStat(((Number) row[2]).longValue(), toLocalDate(row[1])));
        }

        return Map.copyOf(peaks);
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
