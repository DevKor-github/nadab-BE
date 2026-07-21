package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
class AskChatSessionServiceTest {

    @Mock
    private AskChatSessionRepository askChatSessionRepository;

    @Mock
    private AskChatMessageRepository askChatMessageRepository;

    @Mock
    private UserRepository userRepository;

    private AskChatSessionService service;

    @BeforeEach
    void setUp() {
        service = new AskChatSessionService(
                askChatSessionRepository,
                askChatMessageRepository,
                userRepository
        );
    }

    @Test
    void getHome_returns_policy_and_recent_sessions_without_creating_session() {
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime lastMessageAt = OffsetDateTime.of(2026, 7, 14, 10, 3, 0, 0, ZoneOffset.ofHours(9));
        AskChatSession session = session(10L, AskChatSessionStatus.ACTIVE, 2, createdAt, null);
        AskChatMessage firstUserMessage = message(100L, "나는 어떤 사람이야?", createdAt);
        AskChatMessage lastUserMessage = message(
                102L,
                "요즘 내가 놓치고 있는 감정은 뭐야?",
                OffsetDateTime.of(2026, 7, 14, 10, 2, 0, 0, ZoneOffset.ofHours(9))
        );
        AskChatMessage lastMessage = message(103L, "최근에는 안정감을 더 찾는 것 같아요.", lastMessageAt);
        when(askChatSessionRepository.findHistoriesByUserIdAndMessageRole(
                1L,
                AskChatMessageRole.USER,
                PageRequest.of(0, 20)
        )).thenReturn(List.of(session));
        when(askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtAsc(10L, AskChatMessageRole.USER))
                .thenReturn(Optional.of(firstUserMessage));
        when(askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtDesc(10L, AskChatMessageRole.USER))
                .thenReturn(Optional.of(lastUserMessage));
        when(askChatMessageRepository.findFirstBySessionIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(lastMessage));

        var response = service.getHome(1L);

        assertThat(response.maxTurnCount()).isEqualTo(AskChatSessionService.MAX_TURN_COUNT);
        assertThat(response.recentSessionsEmpty()).isFalse();
        assertThat(response.recentSessions()).hasSize(1);
        assertThat(response.recentSessions().get(0).sessionId()).isEqualTo(10L);
        assertThat(response.recentSessions().get(0).title()).isEqualTo("나는 어떤 사람이야?");
        assertThat(response.recentSessions().get(0).lastUserQuestion())
                .isEqualTo("요즘 내가 놓치고 있는 감정은 뭐야?");
        assertThat(response.recentSessions().get(0).lastMessageAt()).isEqualTo(lastMessageAt);
        verifyNoInteractions(userRepository);
    }

    @Test
    void startSession_always_creates_new_session() {
        User user = mock(User.class);
        AskChatSession savedSession = session(
                11L,
                AskChatSessionStatus.ACTIVE,
                0,
                OffsetDateTime.of(2026, 7, 14, 10, 5, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(askChatSessionRepository.save(any(AskChatSession.class))).thenReturn(savedSession);

        var response = service.startSession(1L);

        assertThat(response.sessionId()).isEqualTo(11L);
        assertThat(response.answeredTurnCount()).isZero();
        assertThat(response.remainingTurnCount()).isEqualTo(15);

        ArgumentCaptor<AskChatSession> sessionCaptor = ArgumentCaptor.forClass(AskChatSession.class);
        verify(askChatSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getAnsweredTurnCount()).isZero();
    }

    @Test
    void startSession_rejects_missing_user_when_creating_session() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSession(1L))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(askChatSessionRepository, never()).save(any());
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

    private AskChatMessage message(
            Long id,
            String content,
            OffsetDateTime createdAt
    ) {
        AskChatMessage message = mock(AskChatMessage.class);
        lenient().when(message.getId()).thenReturn(id);
        lenient().when(message.getContent()).thenReturn(content);
        lenient().when(message.getCreatedAt()).thenReturn(createdAt);
        return message;
    }
}
