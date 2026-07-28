package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSampleQuestionRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private AskChatWalletRepository askChatWalletRepository;

    @Mock
    private AskChatSampleQuestionRepository askChatSampleQuestionRepository;

    @Mock
    private UserWalletRepository userWalletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnswerEntryRepository answerEntryRepository;

    @Mock
    private AskChatMessageCommandService askChatMessageCommandService;

    private AskChatSessionService service;

    @BeforeEach
    void setUp() {
        service = new AskChatSessionService(
                askChatSessionRepository,
                askChatWalletRepository,
                askChatSampleQuestionRepository,
                userWalletRepository,
                userRepository,
                answerEntryRepository,
                askChatMessageCommandService
        );
    }

    @Test
    void getHome_returns_home_display_data_without_creating_session() {
        User user = mock(User.class);
        when(user.getNickname()).thenReturn("현진");
        when(answerEntryRepository.countByUserId(1L)).thenReturn(20L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(askChatWalletRepository.findByUserId(1L)).thenReturn(Optional.of(AskChatWallet.create(user, 2, 7)));
        when(userWalletRepository.findByUserId(1L)).thenReturn(Optional.of(UserWallet.create(user, 100L)));
        when(askChatSampleQuestionRepository.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(
                AskChatSampleQuestion.create(InterestCode.VALUES, "나는 어떤 사람이야?", 1),
                AskChatSampleQuestion.create(InterestCode.PREFERENCE, "내가 좋아하는 것들의 공통점은 뭐야?", 2),
                AskChatSampleQuestion.create(InterestCode.RELATIONSHIP, "어떤 사람과 잘 맞을까?", 3)
        ));

        var response = service.getHome(1L);

        assertThat(response.remainingMessageCount()).isEqualTo(9);
        assertThat(response.nickname()).isEqualTo("현진");
        assertThat(response.crystalBalance()).isEqualTo(100L);
        assertThat(response.sampleQuestions()).hasSize(3);
        assertThat(response.sampleQuestions())
                .extracting("category")
                .containsExactlyInAnyOrder("VALUES", "PREFERENCE", "RELATIONSHIP");
        verify(askChatSessionRepository, never()).save(any());
    }

    @Test
    void getHome_rejects_when_answer_count_is_less_than_minimum() {
        when(answerEntryRepository.countByUserId(1L)).thenReturn(19L);

        assertThatThrownBy(() -> service.getHome(1L))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_NOT_ENOUGH_ANSWERS));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(askChatWalletRepository);
        verifyNoInteractions(userWalletRepository);
        verifyNoInteractions(askChatSampleQuestionRepository);
        verify(askChatSessionRepository, never()).save(any());
        verifyNoInteractions(askChatMessageCommandService);
    }

    @Test
    void startSession_creates_new_session_and_sends_first_question() {
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
        AskChatQuestionSendResponse sendResponse = mock(AskChatQuestionSendResponse.class);
        when(askChatMessageCommandService.sendQuestion(1L, 11L, "나는 어떤 사람이야?"))
                .thenReturn(sendResponse);

        var response = service.startSession(1L, "나는 어떤 사람이야?");

        assertThat(response).isSameAs(sendResponse);

        ArgumentCaptor<AskChatSession> sessionCaptor = ArgumentCaptor.forClass(AskChatSession.class);
        verify(askChatSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getAnsweredTurnCount()).isZero();
        verify(askChatMessageCommandService).sendQuestion(1L, 11L, "나는 어떤 사람이야?");
        verifyNoInteractions(answerEntryRepository);
    }

    @Test
    void startSession_rejects_missing_user_when_creating_session() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSession(1L, "나는 어떤 사람이야?"))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(askChatSessionRepository, never()).save(any());
        verifyNoInteractions(askChatMessageCommandService);
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
