package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AskChatMessageReferenceRepository extends JpaRepository<AskChatMessageReference, Long> {

    List<AskChatMessageReference> findAllByMessageIdOrderByDisplayOrderAsc(Long messageId);
}
