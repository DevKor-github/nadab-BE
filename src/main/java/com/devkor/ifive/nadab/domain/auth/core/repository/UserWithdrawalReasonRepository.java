package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.UserWithdrawalReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWithdrawalReasonRepository extends JpaRepository<UserWithdrawalReason, Long> {
}
