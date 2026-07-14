package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatMessageCommandServiceTest {

    @Mock
    private AskChatSessionRepository askChatSessionRepository;

    @Mock
    private AskChatMessageRepository askChatMessageRepository;

    @Mock
    private UserRepository userRepository;

    private AskChatMessageCommandService service;

    @BeforeEach
    void setUp() {
        service = new AskChatMessageCommandService(
                askChatSessionRepository,
                askChatMessageRepository,
                userRepository
        );
    }

    @Test
    void sendQuestion_saves_user_message_to_active_session() {
        AskChatSession activeSession = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                2,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.of(activeSession));
        when(askChatMessageRepository.save(any(AskChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.sendQuestion(1L, "  나는 어떤 사람이야?  ");

        assertThat(response.session().sessionId()).isEqualTo(10L);
        assertThat(response.session().remainingTurnCount()).isEqualTo(13);
        assertThat(response.userMessage().role()).isEqualTo(AskChatMessageRole.USER);
        assertThat(response.userMessage().status()).isEqualTo(AskChatMessageStatus.COMPLETED);
        assertThat(response.userMessage().content()).isEqualTo("나는 어떤 사람이야?");

        ArgumentCaptor<AskChatMessage> messageCaptor = ArgumentCaptor.forClass(AskChatMessage.class);
        verify(askChatMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSession()).isEqualTo(activeSession);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("나는 어떤 사람이야?");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void sendQuestion_creates_session_when_active_session_does_not_exist() {
        User user = mock(User.class);
        AskChatSession savedSession = session(
                11L,
                AskChatSessionStatus.ACTIVE,
                0,
                OffsetDateTime.of(2026, 7, 14, 10, 5, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(askChatSessionRepository.save(any(AskChatSession.class))).thenReturn(savedSession);
        when(askChatMessageRepository.save(any(AskChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.sendQuestion(1L, "궁금한 내용을 적어봐요");

        assertThat(response.session().sessionId()).isEqualTo(11L);
        assertThat(response.session().remainingTurnCount()).isEqualTo(15);
        assertThat(response.userMessage().content()).isEqualTo("궁금한 내용을 적어봐요");

        ArgumentCaptor<AskChatSession> sessionCaptor = ArgumentCaptor.forClass(AskChatSession.class);
        verify(askChatSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getStatus()).isEqualTo(AskChatSessionStatus.ACTIVE);
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
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> service.sendQuestion(1L, "더 물어볼래"))
                .isInstanceOfSatisfying(ConflictException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_TURN_LIMIT_EXCEEDED));
        verify(askChatMessageRepository, never()).save(any());
    }

    @Test
    void sendQuestion_rejects_missing_user_when_creating_session() {
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendQuestion(1L, "나는 어떤 사람이야?"))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(askChatMessageRepository, never()).save(any());
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
