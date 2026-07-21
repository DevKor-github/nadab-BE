package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AskChatWalletRepository extends JpaRepository<AskChatWallet, Long> {

    Optional<AskChatWallet> findByUserId(Long userId);
}
