package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatTurnReservationServiceTest {

    @Mock
    private AskChatWalletRepository askChatWalletRepository;

    private AskChatTurnReservationService service;

    @BeforeEach
    void setUp() {
        service = new AskChatTurnReservationService(askChatWalletRepository);
    }

    @Test
    void ensureReservableTurn_passes_when_wallet_has_free_turn() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 1, 0)));

        assertThatCode(() -> service.ensureReservableTurn(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureReservableTurn_passes_when_wallet_has_paid_turn() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 0, 1)));

        assertThatCode(() -> service.ensureReservableTurn(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureReservableTurn_rejects_when_wallet_is_missing() {
        when(askChatWalletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ensureReservableTurn(1L))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));
    }

    @Test
    void ensureReservableTurn_rejects_when_turn_balance_is_empty() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 0, 0)));

        assertThatThrownBy(() -> service.ensureReservableTurn(1L))
                .isInstanceOfSatisfying(BadRequestException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_TURN_BALANCE_INSUFFICIENT));
    }

    private User user(Long id) {
        User user = User.createUser("test" + id + "@test.com", "hashed");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
