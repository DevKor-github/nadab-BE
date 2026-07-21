package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerGenerationResult;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatGeneratedAnswer;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageReference;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageReferenceRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagDocumentRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatAnswerLlmClient;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatMessageCommandServiceTest {

    @Mock
    private AskChatSessionRepository askChatSessionRepository;

    @Mock
    private AskChatMessageRepository askChatMessageRepository;

    @Mock
    private AskChatMessageReferenceRepository askChatMessageReferenceRepository;

    @Mock
    private AskChatRagDocumentRepository askChatRagDocumentRepository;

    @Mock
    private AskChatAnswerContextService askChatAnswerContextService;

    @Mock
    private AskChatAnswerLlmClient askChatAnswerLlmClient;

    private AskChatMessageCommandService service;
    private AskChatAnswerProperties askChatAnswerProperties;

    @BeforeEach
    void setUp() {
        askChatAnswerProperties = new AskChatAnswerProperties();
        askChatAnswerProperties.setProvider(LlmProvider.OPENAI);
        askChatAnswerProperties.setModel("gpt-4o-mini");
        service = new AskChatMessageCommandService(
                askChatSessionRepository,
                askChatMessageRepository,
                askChatMessageReferenceRepository,
                askChatRagDocumentRepository,
                askChatAnswerContextService,
                askChatAnswerLlmClient,
                askChatAnswerProperties
        );
    }

    @Test
    void sendQuestion_saves_user_message_to_requested_session() {
        AskChatSession activeSession = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                2,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(activeSession));
        when(askChatMessageRepository.save(any(AskChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AskChatAnswerPromptContext context = mock(AskChatAnswerPromptContext.class);
        when(askChatAnswerContextService.build(any(), any(), any())).thenReturn(context);
        when(askChatAnswerLlmClient.generate(context)).thenReturn(successGeneration());
        AskChatRagDocument ragDocument = mock(AskChatRagDocument.class);
        when(askChatRagDocumentRepository.getReferenceById(100L)).thenReturn(ragDocument);

        var response = service.sendQuestion(1L, 10L, "  나는 어떤 사람이야?  ");

        assertThat(response.session().sessionId()).isEqualTo(10L);
        assertThat(response.userMessage().role()).isEqualTo(AskChatMessageRole.USER);
        assertThat(response.userMessage().status()).isEqualTo(AskChatMessageStatus.COMPLETED);
        assertThat(response.userMessage().content()).isEqualTo("나는 어떤 사람이야?");
        assertThat(response.assistantMessage().role()).isEqualTo(AskChatMessageRole.ASSISTANT);
        assertThat(response.assistantMessage().status()).isEqualTo(AskChatMessageStatus.COMPLETED);
        assertThat(response.assistantMessage().content()).isEqualTo("꾸준함이 강점으로 보여요.");
        assertThat(response.followUpQuestions()).containsExactly("언제 꾸준함이 잘 드러났나요?");

        ArgumentCaptor<AskChatMessage> messageCaptor = ArgumentCaptor.forClass(AskChatMessage.class);
        verify(askChatMessageRepository, times(2)).save(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getSession()).isEqualTo(activeSession);
        assertThat(messageCaptor.getAllValues().get(0).getContent()).isEqualTo("나는 어떤 사람이야?");
        assertThat(messageCaptor.getAllValues().get(0).getGenerationDurationMs()).isNull();
        assertThat(messageCaptor.getAllValues().get(1).getStatus()).isEqualTo(AskChatMessageStatus.COMPLETED);
        assertThat(messageCaptor.getAllValues().get(1).getInputTokens()).isEqualTo(10L);
        assertThat(messageCaptor.getAllValues().get(1).getGenerationDurationMs()).isNotNull();
        assertThat(messageCaptor.getAllValues().get(1).getGenerationDurationMs()).isGreaterThanOrEqualTo(0L);
        ArgumentCaptor<AskChatMessageReference> referenceCaptor =
                ArgumentCaptor.forClass(AskChatMessageReference.class);
        verify(askChatMessageReferenceRepository).save(referenceCaptor.capture());
        assertThat(referenceCaptor.getValue().getMessage()).isSameAs(messageCaptor.getAllValues().get(1));
        assertThat(referenceCaptor.getValue().getRagDocument()).isSameAs(ragDocument);
        assertThat(referenceCaptor.getValue().getDisplayOrder()).isEqualTo(1);
        InOrder inOrder = inOrder(askChatAnswerContextService, askChatMessageRepository, askChatAnswerLlmClient);
        inOrder.verify(askChatAnswerContextService).build(any(), any(), any());
        inOrder.verify(askChatMessageRepository).save(messageCaptor.getAllValues().get(0));
        inOrder.verify(askChatAnswerLlmClient).generate(context);
        verify(activeSession).completeAnsweredTurn(AskChatSessionService.MAX_TURN_COUNT);
    }

    @Test
    void sendQuestion_rejects_when_requested_session_does_not_exist_or_not_owned() {
        when(askChatSessionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendQuestion(1L, 99L, "궁금한 내용을 적어봐요"))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_SESSION_NOT_FOUND));
        verify(askChatSessionRepository, never()).save(any());
        verify(askChatMessageRepository, never()).save(any());
    }

    @Test
    void sendQuestion_saves_failed_assistant_message_when_answer_generation_fails() {
        AskChatSession activeSession = session(
                11L,
                AskChatSessionStatus.ACTIVE,
                2,
                OffsetDateTime.of(2026, 7, 14, 10, 5, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findByIdAndUserId(11L, 1L)).thenReturn(Optional.of(activeSession));
        when(askChatMessageRepository.save(any(AskChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AskChatAnswerPromptContext context = mock(AskChatAnswerPromptContext.class);
        when(askChatAnswerContextService.build(any(), any(), any())).thenReturn(context);
        when(askChatAnswerLlmClient.generate(context))
                .thenThrow(new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED));

        var response = service.sendQuestion(1L, 11L, "나는 어떤 사람이야?");

        assertThat(response.userMessage().status()).isEqualTo(AskChatMessageStatus.COMPLETED);
        assertThat(response.assistantMessage().role()).isEqualTo(AskChatMessageRole.ASSISTANT);
        assertThat(response.assistantMessage().status()).isEqualTo(AskChatMessageStatus.FAILED);
        assertThat(response.assistantMessage().content()).isEqualTo("답변 생성에 오류가 발생했어요. 다시 시도해주세요.");
        assertThat(response.followUpQuestions()).isEmpty();

        ArgumentCaptor<AskChatMessage> messageCaptor = ArgumentCaptor.forClass(AskChatMessage.class);
        verify(askChatMessageRepository, times(2)).save(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(1).getLlmProvider()).isEqualTo(LlmProvider.OPENAI);
        assertThat(messageCaptor.getAllValues().get(1).getLlmModel()).isEqualTo("gpt-4o-mini");
        assertThat(messageCaptor.getAllValues().get(1).getErrorCode())
                .isEqualTo(ErrorCode.AI_RESPONSE_PARSE_FAILED.getCode());
        assertThat(messageCaptor.getAllValues().get(1).getGenerationDurationMs()).isNotNull();
        assertThat(messageCaptor.getAllValues().get(1).getGenerationDurationMs()).isGreaterThanOrEqualTo(0L);
        verify(askChatMessageReferenceRepository, never()).save(any());
        verify(activeSession, never()).completeAnsweredTurn(anyInt());
    }

    @Test
    void sendQuestion_rejects_when_turn_limit_is_exceeded() {
        AskChatSession activeSession = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                AskChatSessionService.MAX_TURN_COUNT,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> service.sendQuestion(1L, 10L, "더 물어볼래"))
                .isInstanceOfSatisfying(ConflictException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_TURN_LIMIT_EXCEEDED));
        verify(askChatMessageRepository, never()).save(any());
    }

    @Test
    void sendQuestion_rejects_ended_session() {
        AskChatSession endedSession = session(
                10L,
                AskChatSessionStatus.ENDED,
                3,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2026, 7, 14, 10, 10, 0, 0, ZoneOffset.ofHours(9))
        );
        when(askChatSessionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(endedSession));

        assertThatThrownBy(() -> service.sendQuestion(1L, 10L, "더 물어볼래"))
                .isInstanceOfSatisfying(ConflictException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_TURN_LIMIT_EXCEEDED));
        verify(askChatMessageRepository, never()).save(any());
    }

    @Test
    void sendQuestion_rejects_blank_or_too_long_content_before_session_lookup() {
        assertThatThrownBy(() -> service.sendQuestion(1L, 10L, "   "))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
        assertThatThrownBy(() -> service.sendQuestion(1L, 10L, "가".repeat(201)))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));

        verify(askChatSessionRepository, never()).findByIdAndUserId(any(), any());
        verify(askChatMessageRepository, never()).save(any());
    }

    @Test
    void sendQuestion_rejects_invalid_session_id_before_session_lookup() {
        assertThatThrownBy(() -> service.sendQuestion(1L, null, "나는 어떤 사람이야?"))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
        assertThatThrownBy(() -> service.sendQuestion(1L, 0L, "나는 어떤 사람이야?"))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));

        verify(askChatSessionRepository, never()).findByIdAndUserId(any(), any());
        verify(askChatMessageRepository, never()).save(any());
    }

    private AskChatAnswerGenerationResult successGeneration() {
        return new AskChatAnswerGenerationResult(
                new AskChatGeneratedAnswer(
                        "꾸준함이 강점으로 보여요.",
                        List.of("언제 꾸준함이 잘 드러났나요?")
                ),
                LlmProvider.OPENAI,
                "gpt-4o-mini",
                new LlmTokenUsage(10L, 20L, 30L),
                List.of(100L)
        );
    }

    private AskChatSession session(
            Long id,
            AskChatSessionStatus status,
            int answeredTurnCount,
            OffsetDateTime createdAt,
            OffsetDateTime endedAt
    ) {
        AskChatSession session = mock(AskChatSession.class);
        lenient().when(session.getId()).thenReturn(id);
        lenient().when(session.getStatus()).thenReturn(status);
        lenient().when(session.getAnsweredTurnCount()).thenReturn(answeredTurnCount);
        lenient().when(session.getCreatedAt()).thenReturn(createdAt);
        lenient().when(session.getEndedAt()).thenReturn(endedAt);
        return session;
    }
}
