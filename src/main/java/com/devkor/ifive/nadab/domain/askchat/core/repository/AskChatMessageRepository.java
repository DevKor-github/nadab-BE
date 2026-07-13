package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AskChatMessageRepository extends JpaRepository<AskChatMessage, Long> {

    List<AskChatMessage> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<AskChatMessage> findAllBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);

    Optional<AskChatMessage> findFirstBySessionIdAndRoleOrderByCreatedAtDesc(
            Long sessionId,
            AskChatMessageRole role
    );

    Optional<AskChatMessage> findFirstBySessionIdAndRoleOrderByCreatedAtAsc(
            Long sessionId,
            AskChatMessageRole role
    );

    long countBySessionIdAndRoleAndStatus(
            Long sessionId,
            AskChatMessageRole role,
            AskChatMessageStatus status
    );
}
