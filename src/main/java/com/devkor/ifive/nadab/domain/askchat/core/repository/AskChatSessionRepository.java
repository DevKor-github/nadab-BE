package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AskChatSessionRepository extends JpaRepository<AskChatSession, Long> {

    Optional<AskChatSession> findByIdAndUserId(Long id, Long userId);

    Optional<AskChatSession> findFirstByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            AskChatSessionStatus status
    );

    List<AskChatSession> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
