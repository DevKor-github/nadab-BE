package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatTurnReservation;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatTurnReservationServiceTest {

    @Mock
    private AskChatWalletRepository askChatWalletRepository;

    @Mock
    private AskChatWalletLogRepository askChatWalletLogRepository;

    private AskChatTurnReservationService service;

    @BeforeEach
    void setUp() {
        service = new AskChatTurnReservationService(
                askChatWalletRepository,
                askChatWalletLogRepository
        );
    }

    @Test
    void reserveTurn_prefers_free_turn_and_creates_pending_log() {
        User user = user(1L);
        AskChatSession session = session(user, 10L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 1, 5)))
                .thenReturn(Optional.of(AskChatWallet.create(user, 0, 5)));
        when(askChatWalletRepository.tryReserveFreeTurn(1L)).thenReturn(1);
        when(askChatWalletLogRepository.save(any(AskChatWalletLog.class))).thenAnswer(invocation -> {
            AskChatWalletLog log = invocation.getArgument(0);
            ReflectionTestUtils.setField(log, "id", 100L);
            return log;
        });

        AskChatTurnReservation reservation = service.reserveTurn(1L, session);

        assertThat(reservation.walletLogId()).isEqualTo(100L);
        assertThat(reservation.freeTurnDelta()).isEqualTo(-1);
        assertThat(reservation.paidTurnDelta()).isZero();
        verify(askChatWalletRepository, never()).tryReservePaidTurn(1L);

        ArgumentCaptor<AskChatWalletLog> logCaptor = ArgumentCaptor.forClass(AskChatWalletLog.class);
        verify(askChatWalletLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo(AskChatWalletLogStatus.PENDING);
        assertThat(logCaptor.getValue().getReason()).isEqualTo(AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME);
        assertThat(logCaptor.getValue().getFreeTurnDelta()).isEqualTo(-1);
        assertThat(logCaptor.getValue().getPaidTurnDelta()).isZero();
        assertThat(logCaptor.getValue().getFreeTurnBalanceAfter()).isZero();
        assertThat(logCaptor.getValue().getPaidTurnBalanceAfter()).isEqualTo(5);
    }

    @Test
    void reserveTurn_uses_paid_turn_when_free_turn_is_empty() {
        User user = user(1L);
        AskChatSession session = session(user, 10L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 0, 2)))
                .thenReturn(Optional.of(AskChatWallet.create(user, 0, 1)));
        when(askChatWalletRepository.tryReserveFreeTurn(1L)).thenReturn(0);
        when(askChatWalletRepository.tryReservePaidTurn(1L)).thenReturn(1);
        when(askChatWalletLogRepository.save(any(AskChatWalletLog.class))).thenAnswer(invocation -> {
            AskChatWalletLog log = invocation.getArgument(0);
            ReflectionTestUtils.setField(log, "id", 101L);
            return log;
        });

        AskChatTurnReservation reservation = service.reserveTurn(1L, session);

        assertThat(reservation.freeTurnDelta()).isZero();
        assertThat(reservation.paidTurnDelta()).isEqualTo(-1);

        ArgumentCaptor<AskChatWalletLog> logCaptor = ArgumentCaptor.forClass(AskChatWalletLog.class);
        verify(askChatWalletLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getFreeTurnBalanceAfter()).isZero();
        assertThat(logCaptor.getValue().getPaidTurnBalanceAfter()).isEqualTo(1);
    }

    @Test
    void reserveTurn_rejects_when_wallet_is_missing() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserveTurn(1L, session(user, 10L)))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));
    }

    @Test
    void reserveTurn_rejects_when_turn_balance_is_empty() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 0, 0)));

        assertThatThrownBy(() -> service.reserveTurn(1L, session(user, 10L)))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_TURN_BALANCE_INSUFFICIENT));
    }

    @Test
    void confirm_marks_reservation_log_confirmed() {
        when(askChatWalletLogRepository.markConfirmed(100L)).thenReturn(1);

        service.confirm(new AskChatTurnReservation(100L, -1, 0));

        verify(askChatWalletLogRepository).markConfirmed(100L);
    }

    @Test
    void refund_restores_free_turn_and_records_refund_log() {
        User user = user(1L);
        AskChatSession session = session(user, 10L);
        AskChatTurnReservation reservation = new AskChatTurnReservation(100L, -1, 0);
        when(askChatWalletRepository.refundFreeTurn(1L)).thenReturn(1);
        when(askChatWalletLogRepository.markRefunded(100L)).thenReturn(1);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 2, 0)));

        service.refund(1L, session, reservation);

        verify(askChatWalletLogRepository).markRefunded(100L);
        ArgumentCaptor<AskChatWalletLog> logCaptor = ArgumentCaptor.forClass(AskChatWalletLog.class);
        verify(askChatWalletLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo(AskChatWalletLogStatus.CONFIRMED);
        assertThat(logCaptor.getValue().getReason()).isEqualTo(AskChatWalletLogReason.ANSWER_FAILURE_REFUND);
        assertThat(logCaptor.getValue().getFreeTurnDelta()).isEqualTo(1);
        assertThat(logCaptor.getValue().getPaidTurnDelta()).isZero();
        assertThat(logCaptor.getValue().getFreeTurnBalanceAfter()).isEqualTo(2);
        assertThat(logCaptor.getValue().getRefType()).isEqualTo("ASK_CHAT_WALLET_LOG");
        assertThat(logCaptor.getValue().getRefId()).isEqualTo(100L);
    }

    private User user(Long id) {
        User user = User.createUser("test" + id + "@test.com", "hashed");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private AskChatSession session(User user, Long id) {
        AskChatSession session = AskChatSession.start(user);
        ReflectionTestUtils.setField(session, "id", id);
        return session;
    }
}
