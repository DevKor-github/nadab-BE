package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerConversationMessage;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagSearchResultDto;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagVectorRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AskChatAnswerContextService {

    private final AskChatMessageRepository askChatMessageRepository;
    private final AskChatEmbeddingClient askChatEmbeddingClient;
    private final AskChatRagVectorRepository askChatRagVectorRepository;
    private final AskChatAnswerProperties properties;

    @Transactional(readOnly = true)
    public AskChatAnswerPromptContext build(Long userId, Long sessionId, String question) {
        List<AskChatAnswerConversationMessage> recentMessages = findRecentMessages(sessionId);
        List<AskChatAnswerReferenceDocument> referenceDocuments = searchReferenceDocuments(userId, question);

        return new AskChatAnswerPromptContext(
                userId,
                sessionId,
                question,
                recentMessages,
                referenceDocuments
        );
    }

    private List<AskChatAnswerConversationMessage> findRecentMessages(Long sessionId) {
        List<AskChatMessage> messages = new ArrayList<>(askChatMessageRepository
                .findAllBySessionIdAndStatusOrderByCreatedAtDesc(
                        sessionId,
                        AskChatMessageStatus.COMPLETED,
                        PageRequest.of(0, properties.getRecentMessageLimit())
                ));
        Collections.reverse(messages);

        return messages.stream()
                .map(message -> new AskChatAnswerConversationMessage(
                        message.getRole(),
                        message.getContent()
                ))
                .toList();
    }

    private List<AskChatAnswerReferenceDocument> searchReferenceDocuments(Long userId, String question) {
        List<Double> queryEmbedding = askChatEmbeddingClient.embed(question);
        List<AskChatRagSearchResultDto> results = askChatRagVectorRepository.search(
                userId,
                null,
                queryEmbedding,
                askChatEmbeddingClient.retrievalLimit()
        );

        return results.stream()
                .map(AskChatAnswerReferenceDocument::from)
                .toList();
    }
}
