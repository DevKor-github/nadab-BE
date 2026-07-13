package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagDocumentRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagVectorRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatRagIndexingServiceTest {

    @Mock
    private AskChatMessageRepository messageRepository;

    @Mock
    private AskChatRagDocumentRepository ragDocumentRepository;

    @Mock
    private AskChatRagVectorRepository ragVectorRepository;

    @Mock
    private AskChatEmbeddingClient embeddingClient;

    private AskChatRagIndexingService service;

    @BeforeEach
    void setUp() {
        service = new AskChatRagIndexingService(
                messageRepository,
                ragDocumentRepository,
                ragVectorRepository,
                embeddingClient
        );
    }

    @Test
    void indexAssistantMessage_saves_rag_document_and_embedding_for_completed_assistant_message() {
        AskChatMessage message = message(10L, AskChatMessageRole.ASSISTANT, AskChatMessageStatus.COMPLETED);
        AskChatRagDocument savedDocument = mock(AskChatRagDocument.class);
        when(savedDocument.getId()).thenReturn(100L);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        when(embeddingClient.version()).thenReturn(1);
        when(embeddingClient.model()).thenReturn("text-embedding-3-small");
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE,
                10L,
                1
        )).thenReturn(false);
        when(ragDocumentRepository.save(any(AskChatRagDocument.class))).thenReturn(savedDocument);
        when(embeddingClient.embed("assistant answer")).thenReturn(List.of(0.1, 0.2, 0.3));

        service.indexAssistantMessage(10L, InterestCode.RELATIONSHIP);

        ArgumentCaptor<AskChatRagDocument> captor = ArgumentCaptor.forClass(AskChatRagDocument.class);
        verify(ragDocumentRepository).save(captor.capture());
        AskChatRagDocument document = captor.getValue();
        assertThat(document.getSourceType()).isEqualTo(AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE);
        assertThat(document.getSourceId()).isEqualTo(10L);
        assertThat(document.getInterestCode()).isEqualTo(InterestCode.RELATIONSHIP);
        assertThat(document.getContent()).isEqualTo("assistant answer");
        assertThat(document.getEmbeddingModel()).isEqualTo("text-embedding-3-small");
        assertThat(document.getEmbeddingVersion()).isEqualTo(1);
        assertThat(document.getMetadata())
                .containsEntry("sessionId", 20L)
                .containsEntry("messageId", 10L)
                .containsEntry("role", "ASSISTANT")
                .containsEntry("status", "COMPLETED")
                .containsEntry("llmProvider", "OPENAI")
                .containsEntry("llmModel", "gpt-4o-mini");
        verify(ragVectorRepository).updateEmbedding(100L, List.of(0.1, 0.2, 0.3));
    }

    @Test
    void indexAssistantMessage_skips_non_completed_assistant_message() {
        AskChatMessage message = message(10L, AskChatMessageRole.USER, AskChatMessageStatus.COMPLETED);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        service.indexAssistantMessage(10L, InterestCode.RELATIONSHIP);

        verifyNoInteractions(ragDocumentRepository, ragVectorRepository, embeddingClient);
    }

    @Test
    void indexAssistantMessage_skips_duplicate_document_version() {
        AskChatMessage message = message(10L, AskChatMessageRole.ASSISTANT, AskChatMessageStatus.COMPLETED);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        when(embeddingClient.version()).thenReturn(1);
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE,
                10L,
                1
        )).thenReturn(true);

        service.indexAssistantMessage(10L, InterestCode.RELATIONSHIP);

        verify(ragDocumentRepository, never()).save(any());
        verifyNoInteractions(ragVectorRepository);
    }

    @Test
    void indexAssistantMessage_marks_document_failed_when_embedding_fails() {
        AskChatMessage message = message(10L, AskChatMessageRole.ASSISTANT, AskChatMessageStatus.COMPLETED);
        AskChatRagDocument savedDocument = mock(AskChatRagDocument.class);
        when(savedDocument.getId()).thenReturn(100L);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        when(embeddingClient.version()).thenReturn(1);
        when(embeddingClient.model()).thenReturn("text-embedding-3-small");
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE,
                10L,
                1
        )).thenReturn(false);
        when(ragDocumentRepository.save(any(AskChatRagDocument.class))).thenReturn(savedDocument);
        when(embeddingClient.embed("assistant answer")).thenThrow(new IllegalStateException("embedding failed"));

        service.indexAssistantMessage(10L, InterestCode.RELATIONSHIP);

        verify(ragVectorRepository).markEmbeddingFailed(100L, "IllegalStateException");
        verify(ragVectorRepository, never()).updateEmbedding(any(), any());
    }

    @Test
    void indexAssistantMessage_rejects_missing_message() {
        when(messageRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.indexAssistantMessage(10L, InterestCode.RELATIONSHIP))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_MESSAGE_NOT_FOUND));
    }

    private AskChatMessage message(Long id, AskChatMessageRole role, AskChatMessageStatus status) {
        User user = mock(User.class);
        AskChatSession session = mock(AskChatSession.class);
        lenient().when(session.getId()).thenReturn(20L);
        lenient().when(session.getUser()).thenReturn(user);

        AskChatMessage message = mock(AskChatMessage.class);
        lenient().when(message.getId()).thenReturn(id);
        lenient().when(message.getSession()).thenReturn(session);
        lenient().when(message.getRole()).thenReturn(role);
        lenient().when(message.getStatus()).thenReturn(status);
        lenient().when(message.getContent()).thenReturn("assistant answer");
        lenient().when(message.getLlmProvider()).thenReturn(LlmProvider.OPENAI);
        lenient().when(message.getLlmModel()).thenReturn("gpt-4o-mini");
        return message;
    }
}
