package com.devkor.ifive.nadab.domain.ads.core.repository;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdRewardSessionRepository extends JpaRepository<AdRewardSession, Long> {

    // SSV 콜백 지급 처리용: 동시 콜백을 직렬화하는 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM AdRewardSession s
         WHERE s.sessionKey = :sessionKey
    """)
    Optional<AdRewardSession> findBySessionKeyForUpdate(@Param("sessionKey") String sessionKey);

    // 폴링 조회용
    Optional<AdRewardSession> findBySessionKey(String sessionKey);

    boolean existsByTransactionId(String transactionId);

    // 활성 세션 1개: 세션 발급 전 같은 유저의 PENDING 세션을 만료 처리(동시세션 과지급·이중차감 차단)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdRewardSession s
           SET s.status = com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus.EXPIRED,
               s.updatedAt = CURRENT_TIMESTAMP
         WHERE s.user.id = :userId
           AND s.status = com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus.PENDING
    """)
    int expirePendingSessions(@Param("userId") Long userId);
}