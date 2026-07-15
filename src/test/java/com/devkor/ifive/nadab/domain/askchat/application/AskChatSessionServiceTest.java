package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
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
    void getHome_returns_active_session_when_exists() {
        AskChatSession activeSession = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                4,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.of(activeSession));

        var response = service.getHome(1L);

        assertThat(response.maxTurnCount()).isEqualTo(AskChatSessionService.MAX_TURN_COUNT);
        assertThat(response.remainingTurnCount()).isEqualTo(11);
        assertThat(response.activeSession()).isNotNull();
        assertThat(response.activeSession().sessionId()).isEqualTo(10L);
        assertThat(response.activeSession().status()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(response.activeSession().answeredTurnCount()).isEqualTo(4);
        verify(userRepository, never()).findById(any());
        verify(askChatSessionRepository, never()).save(any());
    }

    @Test
    void getHome_does_not_create_session_when_active_session_does_not_exist() {
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.empty());

        var response = service.getHome(1L);

        assertThat(response.maxTurnCount()).isEqualTo(AskChatSessionService.MAX_TURN_COUNT);
        assertThat(response.remainingTurnCount()).isEqualTo(15);
        assertThat(response.activeSession()).isNull();
        verify(userRepository, never()).findById(any());
        verify(askChatSessionRepository, never()).save(any());
    }

    @Test
    void startSession_returns_active_session_when_exists() {
        AskChatSession activeSession = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                4,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.of(activeSession));

        var response = service.startSession(1L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(response.answeredTurnCount()).isEqualTo(4);
        assertThat(response.maxTurnCount()).isEqualTo(AskChatSessionService.MAX_TURN_COUNT);
        assertThat(response.remainingTurnCount()).isEqualTo(11);
        verify(userRepository, never()).findById(any());
        verify(askChatSessionRepository, never()).save(any());
    }

    @Test
    void startSession_creates_session_when_active_session_does_not_exist() {
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

        var response = service.startSession(1L);

        assertThat(response.sessionId()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(response.answeredTurnCount()).isZero();
        assertThat(response.remainingTurnCount()).isEqualTo(15);

        ArgumentCaptor<AskChatSession> sessionCaptor = ArgumentCaptor.forClass(AskChatSession.class);
        verify(askChatSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getStatus()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(sessionCaptor.getValue().getAnsweredTurnCount()).isZero();
    }

    @Test
    void startSession_rejects_missing_user_when_creating_session() {
        when(askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                1L,
                AskChatSessionStatus.ACTIVE
        )).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSession(1L))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(askChatSessionRepository, never()).save(any());
    }

    @Test
    void restartSession_ends_active_session_and_creates_new_session() {
        User user = mock(User.class);
        AskChatSession activeSession = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                4,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                null
        );
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
        )).thenReturn(Optional.of(activeSession));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(askChatSessionRepository.save(any(AskChatSession.class))).thenReturn(savedSession);

        var response = service.restartSession(1L);

        verify(activeSession).end();
        assertThat(response.sessionId()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(response.answeredTurnCount()).isZero();
        assertThat(response.remainingTurnCount()).isEqualTo(15);
    }

    @Test
    void restartSession_creates_session_when_active_session_does_not_exist() {
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

        var response = service.restartSession(1L);

        assertThat(response.sessionId()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(response.answeredTurnCount()).isZero();
        assertThat(response.remainingTurnCount()).isEqualTo(15);
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
