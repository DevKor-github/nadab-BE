package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface WithdrawalEventRepository extends JpaRepository<WithdrawalEvent, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from WithdrawalEvent event
            where event.expiresAt <= :expirationDate
            """)
    int deleteExpiredEvents(@Param("expirationDate") OffsetDateTime expirationDate);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update WithdrawalEvent event
            set event.user = null,
                event.anonymizedAt = :anonymizedAt
            where event.withdrawnAt < :anonymizationDate
              and (event.user is not null or event.anonymizedAt is null)
            """)
    int anonymizeEventsWithdrawnBefore(
            @Param("anonymizationDate") OffsetDateTime anonymizationDate,
            @Param("anonymizedAt") OffsetDateTime anonymizedAt
    );
}
