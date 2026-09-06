package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.UserWithdrawalReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface UserWithdrawalReasonRepository extends JpaRepository<UserWithdrawalReason, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from UserWithdrawalReason reason
            where reason.withdrawnAt < :anonymizationDate
            """)
    int deleteReasonsWithdrawnBefore(
            @Param("anonymizationDate") OffsetDateTime anonymizationDate
    );
}
