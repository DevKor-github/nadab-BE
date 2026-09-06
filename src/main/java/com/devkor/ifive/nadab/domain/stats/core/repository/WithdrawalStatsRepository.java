package com.devkor.ifive.nadab.domain.stats.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WithdrawalStatsRepository {

    private final EntityManager em;

    public List<Object[]> findLatestRetainedWithdrawalReasonRows(int limitEvents) {
        return em.createNativeQuery("""
                with ranked_events as (
                    select
                        we.id,
                        we.withdrawn_at,
                        row_number() over (order by we.withdrawn_at desc, we.id desc) as rn
                    from withdrawal_events we
                    where we.expires_at > current_timestamp
                )
                select
                    re.id,
                    re.withdrawn_at,
                    wer.reason,
                    case when we.user_id is null then null else wer.custom_reason end
                from ranked_events re
                join withdrawal_events we on we.id = re.id
                join withdrawal_event_reasons wer on wer.event_id = re.id
                where re.rn <= :limitEvents
                order by re.withdrawn_at desc, re.id desc, wer.reason asc
                """)
                .setParameter("limitEvents", limitEvents)
                .getResultList();
    }

    public List<Object[]> countRetainedWithdrawalReasons() {
        return em.createNativeQuery("""
                select
                    wer.reason,
                    count(*) as cnt
                from withdrawal_event_reasons wer
                join withdrawal_events we on we.id = wer.event_id
                where we.expires_at > current_timestamp
                group by wer.reason
                """)
                .getResultList();
    }
}
