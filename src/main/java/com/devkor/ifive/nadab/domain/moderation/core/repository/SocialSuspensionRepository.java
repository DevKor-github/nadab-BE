package com.devkor.ifive.nadab.domain.moderation.core.repository;

import com.devkor.ifive.nadab.domain.moderation.core.entity.SocialSuspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SocialSuspensionRepository extends JpaRepository<SocialSuspension, Long> {

    boolean existsByUserIdAndExpiresAtAfter(Long userId, OffsetDateTime now);

    @Query("""
        SELECT DISTINCT ss.user.id
        FROM SocialSuspension ss
        WHERE ss.user.id IN :userIds
          AND ss.expiresAt > :now
        """)
    List<Long> findActiveSuspendedUserIds(@Param("userIds") List<Long> userIds,
                                          @Param("now") OffsetDateTime now);

    @Query("""
        SELECT DISTINCT ss.user.id
        FROM SocialSuspension ss
        WHERE ss.expiresAt > :now
        """)
    List<Long> findAllActiveSuspendedUserIds(@Param("now") OffsetDateTime now);

    /**
     * 가장 최근 정지 레코드 조회 — 신고 누적 기준점(expires_at)으로 사용
     */
    Optional<SocialSuspension> findFirstByUserIdOrderByStartedAtDesc(Long userId);
}