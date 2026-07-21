package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AskChatWalletRepository extends JpaRepository<AskChatWallet, Long> {

    Optional<AskChatWallet> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AskChatWallet w
        SET w.paidTurnBalance = w.paidTurnBalance + :amount,
            w.version = w.version + 1
        WHERE w.user.id = :userId
    """)
    int chargePaidTurns(@Param("userId") Long userId, @Param("amount") int amount);
}
