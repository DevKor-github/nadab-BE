package com.devkor.ifive.nadab.domain.test.core.repository;

import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TestCrystalLogRepository extends JpaRepository<CrystalLog, Long> {

    // 환불되지 않은 결제 로그 1건을 찾기 (중복 환불 방지의 기준)
    Optional<CrystalLog> findByUserIdAndRefTypeAndRefIdAndReasonAndStatus(
            Long user_id, String refType, Long refId, CrystalLogReason reason, CrystalLogStatus status
    );

    @Modifying
    @Query("UPDATE CrystalLog l SET l.status = com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus.REFUNDED WHERE l.id = :logId")
    int markRefunded(@Param("logId") Long logId);
}
