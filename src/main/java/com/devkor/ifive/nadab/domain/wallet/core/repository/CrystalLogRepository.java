package com.devkor.ifive.nadab.domain.wallet.core.repository;

import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrystalLogRepository extends JpaRepository<CrystalLog,Long> {

    @Query(value = """
            SELECT l
            FROM CrystalLog l
            LEFT JOIN FETCH l.user u
            WHERE (COALESCE(:nickname, '') = ''
                   OR LOWER(u.nickname) LIKE CONCAT('%', COALESCE(:nickname, ''), '%'))
              AND (COALESCE(:email, '') = ''
                   OR LOWER(u.email) LIKE CONCAT('%', COALESCE(:email, ''), '%'))
            ORDER BY l.createdAt DESC, l.id DESC
            """,
            countQuery = """
            SELECT COUNT(l)
            FROM CrystalLog l
            LEFT JOIN l.user u
            WHERE (COALESCE(:nickname, '') = ''
                   OR LOWER(u.nickname) LIKE CONCAT('%', COALESCE(:nickname, ''), '%'))
              AND (COALESCE(:email, '') = ''
                   OR LOWER(u.email) LIKE CONCAT('%', COALESCE(:email, ''), '%'))
            """)
    Page<CrystalLog> findAllForAdmin(
            @Param("nickname") String nickname,
            @Param("email") String email,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE CrystalLog l SET l.status = com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus.CONFIRMED WHERE l.id = :logId")
    int markConfirmed(@Param("logId") Long logId);

    @Modifying
    @Query("UPDATE CrystalLog l SET l.status = com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus.REFUNDED WHERE l.id = :logId")
    int markRefunded(@Param("logId") Long logId);
}
