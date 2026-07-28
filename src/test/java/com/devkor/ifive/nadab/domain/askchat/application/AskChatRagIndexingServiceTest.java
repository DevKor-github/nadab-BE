package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagEmbeddingStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagDocumentRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagVectorRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
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
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
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
    private AnswerEntryRepository answerEntryRepository;

    @Mock
    private DailyReportRepository dailyReportRepository;

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
                answerEntryRepository,
                dailyReportRepository,
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
    void indexDailyAnswer_saves_rag_document_and_embedding_for_completed_daily_report() {
        AnswerEntry answerEntry = answerEntry(30L);
        DailyReport report = dailyReport(100L, answerEntry, DailyReportStatus.COMPLETED);
        AskChatRagDocument savedDocument = mock(AskChatRagDocument.class);
        when(savedDocument.getId()).thenReturn(200L);
        when(answerEntryRepository.findById(30L)).thenReturn(Optional.of(answerEntry));
        when(dailyReportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(embeddingClient.version()).thenReturn(1);
        when(embeddingClient.model()).thenReturn("text-embedding-3-small");
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ANSWER_ENTRY,
                30L,
                1
        )).thenReturn(false);
        when(ragDocumentRepository.save(any(AskChatRagDocument.class))).thenReturn(savedDocument);
        String content = """
                질문: question
                사용자 답변: answer
                수정구슬 답변: report
                """.trim();
        when(embeddingClient.embed(content)).thenReturn(List.of(0.4, 0.5, 0.6));

        service.indexDailyAnswer(30L, 100L, InterestCode.RELATIONSHIP);

        ArgumentCaptor<AskChatRagDocument> captor = ArgumentCaptor.forClass(AskChatRagDocument.class);
        verify(ragDocumentRepository).save(captor.capture());
        AskChatRagDocument document = captor.getValue();
        assertThat(document.getSourceType()).isEqualTo(AskChatRagDocumentSourceType.ANSWER_ENTRY);
        assertThat(document.getSourceId()).isEqualTo(30L);
        assertThat(document.getInterestCode()).isEqualTo(InterestCode.RELATIONSHIP);
        assertThat(document.getContent()).isEqualTo(content);
        assertThat(document.getEmbeddingModel()).isEqualTo("text-embedding-3-small");
        assertThat(document.getEmbeddingVersion()).isEqualTo(1);
        assertThat(document.getMetadata())
                .containsEntry("answerEntryId", 30L)
                .containsEntry("dailyReportId", 100L)
                .containsEntry("questionId", 40L)
                .containsEntry("date", "2026-07-13")
                .containsEntry("source", "DAILY_REPORT_COMPLETED")
                .containsEntry("emotion", "ACHIEVEMENT");
        verify(ragVectorRepository).updateEmbedding(200L, List.of(0.4, 0.5, 0.6));
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
        when(embeddingClient.maxRetryCount()).thenReturn(3);
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ASK_CHAT_MESSAGE,
                10L,
                1
        )).thenReturn(false);
        when(ragDocumentRepository.save(any(AskChatRagDocument.class))).thenReturn(savedDocument);
        when(embeddingClient.embed("assistant answer")).thenThrow(new IllegalStateException("embedding failed"));

        service.indexAssistantMessage(10L, InterestCode.RELATIONSHIP);

        verify(ragVectorRepository).markEmbeddingFailed(100L, "IllegalStateException", 3);
        verify(ragVectorRepository, never()).updateEmbedding(any(), any());
    }

    @Test
    void indexDailyAnswer_skips_duplicate_document_version() {
        AnswerEntry answerEntry = answerEntry(30L);
        DailyReport report = dailyReport(100L, answerEntry, DailyReportStatus.COMPLETED);
        when(answerEntryRepository.findById(30L)).thenReturn(Optional.of(answerEntry));
        when(dailyReportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(embeddingClient.version()).thenReturn(1);
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ANSWER_ENTRY,
                30L,
                1
        )).thenReturn(true);

        service.indexDailyAnswer(30L, 100L, InterestCode.RELATIONSHIP);

        verify(ragDocumentRepository, never()).save(any());
        verifyNoInteractions(ragVectorRepository);
    }

    @Test
    void indexDailyAnswer_marks_document_failed_when_embedding_fails() {
        AnswerEntry answerEntry = answerEntry(30L);
        DailyReport report = dailyReport(100L, answerEntry, DailyReportStatus.COMPLETED);
        AskChatRagDocument savedDocument = mock(AskChatRagDocument.class);
        when(savedDocument.getId()).thenReturn(200L);
        when(answerEntryRepository.findById(30L)).thenReturn(Optional.of(answerEntry));
        when(dailyReportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(embeddingClient.version()).thenReturn(1);
        when(embeddingClient.model()).thenReturn("text-embedding-3-small");
        when(embeddingClient.maxRetryCount()).thenReturn(3);
        when(ragDocumentRepository.existsBySourceTypeAndSourceIdAndEmbeddingVersion(
                AskChatRagDocumentSourceType.ANSWER_ENTRY,
                30L,
                1
        )).thenReturn(false);
        when(ragDocumentRepository.save(any(AskChatRagDocument.class))).thenReturn(savedDocument);
        when(embeddingClient.embed(any())).thenThrow(new IllegalStateException("embedding failed"));

        service.indexDailyAnswer(30L, 100L, InterestCode.RELATIONSHIP);

        verify(ragVectorRepository).markEmbeddingFailed(200L, "IllegalStateException", 3);
        verify(ragVectorRepository, never()).updateEmbedding(any(), any());
    }

    @Test
    void retryFailedEmbeddings_retries_failed_documents_by_batch_size() {
        AskChatRagDocument first = ragDocument(1L, "first content");
        AskChatRagDocument second = ragDocument(2L, "second content");
        when(embeddingClient.batchSize()).thenReturn(20);
        when(embeddingClient.maxRetryCount()).thenReturn(3);
        when(ragDocumentRepository.findAllByEmbeddingStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                AskChatRagEmbeddingStatus.FAILED,
                3,
                PageRequest.of(0, 20)
        )).thenReturn(List.of(first, second));
        when(embeddingClient.embed("first content")).thenReturn(List.of(0.1, 0.2));
        when(embeddingClient.embed("second content")).thenReturn(List.of(0.3, 0.4));

        int retriedCount = service.retryFailedEmbeddings();

        assertThat(retriedCount).isEqualTo(2);
        verify(ragVectorRepository).updateEmbedding(1L, List.of(0.1, 0.2));
        verify(ragVectorRepository).updateEmbedding(2L, List.of(0.3, 0.4));
    }

    @Test
    void retryFailedEmbeddings_keeps_document_failed_when_embedding_fails() {
        AskChatRagDocument document = ragDocument(1L, "content");
        when(embeddingClient.batchSize()).thenReturn(20);
        when(embeddingClient.maxRetryCount()).thenReturn(3);
        when(ragDocumentRepository.findAllByEmbeddingStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                AskChatRagEmbeddingStatus.FAILED,
                3,
                PageRequest.of(0, 20)
        )).thenReturn(List.of(document));
        when(embeddingClient.embed("content")).thenThrow(new IllegalStateException("embedding failed"));

        int retriedCount = service.retryFailedEmbeddings();

        assertThat(retriedCount).isEqualTo(1);
        verify(ragVectorRepository).markEmbeddingFailed(1L, "IllegalStateException", 3);
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

    private AnswerEntry answerEntry(Long id) {
        User user = mock(User.class);
        DailyQuestion question = mock(DailyQuestion.class);
        lenient().when(question.getId()).thenReturn(40L);
        lenient().when(question.getQuestionText()).thenReturn("question");

        AnswerEntry answerEntry = mock(AnswerEntry.class);
        lenient().when(answerEntry.getId()).thenReturn(id);
        lenient().when(answerEntry.getUser()).thenReturn(user);
        lenient().when(answerEntry.getQuestion()).thenReturn(question);
        lenient().when(answerEntry.getContent()).thenReturn("answer");
        lenient().when(answerEntry.getDate()).thenReturn(LocalDate.of(2026, 7, 13));
        return answerEntry;
    }

    private DailyReport dailyReport(Long id, AnswerEntry answerEntry, DailyReportStatus status) {
        Emotion emotion = mock(Emotion.class);
        lenient().when(emotion.getCode()).thenReturn(EmotionCode.ACHIEVEMENT);

        DailyReport report = mock(DailyReport.class);
        lenient().when(report.getId()).thenReturn(id);
        lenient().when(report.getAnswerEntry()).thenReturn(answerEntry);
        lenient().when(report.getStatus()).thenReturn(status);
        lenient().when(report.getContent()).thenReturn("report");
        lenient().when(report.getEmotion()).thenReturn(emotion);
        return report;
    }

    private AskChatRagDocument ragDocument(Long id, String content) {
        AskChatRagDocument document = mock(AskChatRagDocument.class);
        lenient().when(document.getId()).thenReturn(id);
        lenient().when(document.getContent()).thenReturn(content);
        return document;
    }
}
