package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalEventReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface WithdrawalEventReasonRepository extends JpaRepository<WithdrawalEventReason, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update withdrawal_event_reasons reason
            set custom_reason = null
            from withdrawal_events event
            where event.id = reason.event_id
              and event.withdrawn_at < :anonymizationDate
              and reason.custom_reason is not null
            """, nativeQuery = true)
    int clearCustomReasonsWithdrawnBefore(
            @Param("anonymizationDate") OffsetDateTime anonymizationDate
    );
}
