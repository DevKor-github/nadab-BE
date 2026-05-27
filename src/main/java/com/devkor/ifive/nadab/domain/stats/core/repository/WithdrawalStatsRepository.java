package com.devkor.ifive.nadab.domain.stats.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WithdrawalStatsRepository {

    private final EntityManager em;

    public List<Object[]> findLatestWithdrawalReasonRows(int limitEvents) {
        return em.createNativeQuery("""
                with ranked_events as (
                    select
                        uwr.user_id,
                        uwr.withdrawn_at,
                        row_number() over (order by uwr.withdrawn_at desc, uwr.user_id desc) as rn
                    from user_withdrawal_reasons uwr
                    group by uwr.user_id, uwr.withdrawn_at
                )
                select
                    uwr.user_id,
                    uwr.withdrawn_at,
                    uwr.reason,
                    uwr.custom_reason
                from user_withdrawal_reasons uwr
                join ranked_events re
                  on re.user_id = uwr.user_id
                 and re.withdrawn_at = uwr.withdrawn_at
                where re.rn <= :limitEvents
                order by uwr.withdrawn_at desc, uwr.user_id desc, uwr.reason asc
                """)
                .setParameter("limitEvents", limitEvents)
                .getResultList();
    }

    public List<Object[]> countAllWithdrawalReasons() {
        return em.createNativeQuery("""
                select
                    uwr.reason,
                    count(*) as cnt
                from user_withdrawal_reasons uwr
                group by uwr.reason
                """)
                .getResultList();
    }
}
