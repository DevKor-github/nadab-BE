package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AskChatWalletLogRepository extends JpaRepository<AskChatWalletLog, Long> {

    List<AskChatWalletLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = """
            SELECT l
            FROM AskChatWalletLog l
            LEFT JOIN FETCH l.user u
            WHERE (COALESCE(:nickname, '') = ''
                   OR LOWER(u.nickname) LIKE CONCAT('%', COALESCE(:nickname, ''), '%'))
              AND (COALESCE(:email, '') = ''
                   OR LOWER(u.email) LIKE CONCAT('%', COALESCE(:email, ''), '%'))
            ORDER BY l.createdAt DESC, l.id DESC
            """,
            countQuery = """
            SELECT COUNT(l)
            FROM AskChatWalletLog l
            LEFT JOIN l.user u
            WHERE (COALESCE(:nickname, '') = ''
                   OR LOWER(u.nickname) LIKE CONCAT('%', COALESCE(:nickname, ''), '%'))
              AND (COALESCE(:email, '') = ''
                   OR LOWER(u.email) LIKE CONCAT('%', COALESCE(:email, ''), '%'))
            """)
    Page<AskChatWalletLog> findAllForAdmin(
            @Param("nickname") String nickname,
            @Param("email") String email,
            Pageable pageable
    );

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
