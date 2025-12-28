package com.devkor.ifive.nadab.domain.wallet.core.repository;

import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrystalLogRepository extends JpaRepository<CrystalLog,Long> {

    @Modifying
    @Query("UPDATE CrystalLog l SET l.status = com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus.CONFIRMED WHERE l.id = :logId")
    int markConfirmed(@Param("logId") Long logId);

    @Modifying
    @Query("UPDATE CrystalLog l SET l.status = com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus.REFUNDED WHERE l.id = :logId")
    int markRefunded(@Param("logId") Long logId);
}
