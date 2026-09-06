package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalEventReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalEventReasonRepository extends JpaRepository<WithdrawalEventReason, Long> {
}
