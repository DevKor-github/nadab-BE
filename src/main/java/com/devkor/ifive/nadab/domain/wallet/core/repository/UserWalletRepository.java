package com.devkor.ifive.nadab.domain.wallet.core.repository;

import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserWalletRepository extends JpaRepository<UserWallet, Integer> {

    Optional<UserWallet> findByUserId(Long userId);
}
