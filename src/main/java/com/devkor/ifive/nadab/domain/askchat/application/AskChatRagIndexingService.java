package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagDocumentRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagVectorRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AskChatRagIndexingService {

    private static final int ERROR_CODE_MAX_LENGTH = 128;

    private final AskChatMessageRepository messageRepository;
    private final AskChatRagDocumentRepository ragDocumentRepository;
    private final AskChatRagVectorRepository ragVectorRepository;
    private final AskChatEmbeddingClient embeddingClient;

    @Transactional
    public void indexAssistantMessage(Long messageId, InterestCode interestCode) {
        AskChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_MESSAGE_NOT_FOUND));

        if (!isIndexableAssistantMessage(message)) {
            return;
        }

        int embeddingVersion = embeddingClient.version();
        if (ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE,
                message.getId(),
                embeddingVersion
        )) {
            return;
        }

        AskChatRagDocument document = ragDocumentRepository.save(AskChatRagDocument.createPending(
                message.getSession().getUser(),
                AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE,
                message.getId(),
                interestCode,
                message.getContent(),
                metadata(message),
                embeddingClient.model(),
                embeddingVersion
        ));

        try {
            List<Double> embedding = embeddingClient.embed(message.getContent());
            ragVectorRepository.updateEmbedding(document.getId(), embedding);
        } catch (RuntimeException e) {
            ragVectorRepository.markEmbeddingFailed(document.getId(), truncate(e.getClass().getSimpleName()));
        }
    }

    private boolean isIndexableAssistantMessage(AskChatMessage message) {
        return message.getRole() == AskChatMessageRole.ASSISTANT
                && message.getStatus() == AskChatMessageStatus.COMPLETED;
    }

    private Map<String, Object> metadata(AskChatMessage message) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sessionId", message.getSession().getId());
        metadata.put("messageId", message.getId());
        metadata.put("role", message.getRole().name());
        metadata.put("status", message.getStatus().name());
        if (message.getLlmProvider() != null) {
            metadata.put("llmProvider", message.getLlmProvider().name());
        }
        if (message.getLlmModel() != null) {
            metadata.put("llmModel", message.getLlmModel());
        }
        return metadata;
    }

    private String truncate(String value) {
        if (value.length() <= ERROR_CODE_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, ERROR_CODE_MAX_LENGTH);
    }
}
