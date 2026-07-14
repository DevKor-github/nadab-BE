package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AskChatSessionRepository extends JpaRepository<AskChatSession, Long> {

    Optional<AskChatSession> findByIdAndUserId(Long id, Long userId);

    Optional<AskChatSession> findFirstByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            AskChatSessionStatus status
    );

    List<AskChatSession> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Query("""
        select s
        from AskChatSession s
        where s.user.id = :userId
          and exists (
              select 1
              from AskChatMessage m
              where m.session = s
                and m.role = :role
          )
        order by s.createdAt desc
        """)
    List<AskChatSession> findHistoriesByUserIdAndMessageRole(
            @Param("userId") Long userId,
            @Param("role") AskChatMessageRole role,
            Pageable pageable
    );

    @Query("""
        select count(s)
        from AskChatSession s
        where s.user.id = :userId
          and exists (
              select 1
              from AskChatMessage m
              where m.session = s
                and m.role = :role
          )
        """)
    long countHistoriesByUserIdAndMessageRole(
            @Param("userId") Long userId,
            @Param("role") AskChatMessageRole role
    );
}
