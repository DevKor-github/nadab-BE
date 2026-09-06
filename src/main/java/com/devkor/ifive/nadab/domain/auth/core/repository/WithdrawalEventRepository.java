package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalEventRepository extends JpaRepository<WithdrawalEvent, Long> {
}
