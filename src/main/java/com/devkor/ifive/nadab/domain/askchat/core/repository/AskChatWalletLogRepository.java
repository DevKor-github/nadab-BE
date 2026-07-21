package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AskChatWalletLogRepository extends JpaRepository<AskChatWalletLog, Long> {

    List<AskChatWalletLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
