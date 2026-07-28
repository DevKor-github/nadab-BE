package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AskChatWalletLogRepository extends JpaRepository<AskChatWalletLog, Long> {

    List<AskChatWalletLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AskChatWalletLog l
        SET l.status = com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus.CONFIRMED
        WHERE l.id = :logId
    """)
    int markConfirmed(@Param("logId") Long logId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AskChatWalletLog l
        SET l.status = com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus.REFUNDED
        WHERE l.id = :logId
    """)
    int markRefunded(@Param("logId") Long logId);
}
