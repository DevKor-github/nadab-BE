package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatSessionServiceTest {

    @Mock
    private AskChatSessionRepository askChatSessionRepository;

    @Mock
    private UserRepository userRepository;

    private AskChatSessionService service;

    @BeforeEach
    void setUp() {
        service = new AskChatSessionService(
                askChatSessionRepository,
                userRepository
        );
    }

    @Test
    void getHome_returns_policy_without_loading_or_creating_session() {
        var response = service.getHome(1L);

        assertThat(response.maxTurnCount()).isEqualTo(AskChatSessionService.MAX_TURN_COUNT);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(askChatSessionRepository);
    }

    @Test
    void startSession_always_creates_new_session() {
        User user = mock(User.class);
        AskChatSession savedSession = session(
                11L,
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
            int answeredTurnCount,
            OffsetDateTime createdAt,
            OffsetDateTime endedAt
    ) {
        AskChatSession session = mock(AskChatSession.class);
        lenient().when(session.getId()).thenReturn(id);
        lenient().when(session.getAnsweredTurnCount()).thenReturn(answeredTurnCount);
        lenient().when(session.getCreatedAt()).thenReturn(createdAt);
        lenient().when(session.getEndedAt()).thenReturn(endedAt);
        return session;
    }
}
