package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagEmbeddingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AskChatRagDocumentRepository extends JpaRepository<AskChatRagDocument, Long> {

    Optional<AskChatRagDocument> findBySourceTypeAndSourceIdAndEmbeddingVersion(
            AskChatRagDocumentSourceType sourceType,
            Long sourceId,
            int embeddingVersion
    );

    boolean existsBySourceTypeAndSourceIdAndEmbeddingVersion(
            AskChatRagDocumentSourceType sourceType,
            Long sourceId,
            int embeddingVersion
    );

    List<AskChatRagDocument> findAllByEmbeddingStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            AskChatRagEmbeddingStatus embeddingStatus,
            int retryCount,
            Pageable pageable
    );
}
