package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatWalletChargeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserWalletRepository userWalletRepository;

    @Mock
    private CrystalLogRepository crystalLogRepository;

    @Mock
    private AskChatWalletRepository askChatWalletRepository;

    @Mock
    private AskChatWalletLogRepository askChatWalletLogRepository;

    private AskChatWalletChargeService service;

    @BeforeEach
    void setUp() {
        service = new AskChatWalletChargeService(
                userRepository,
                userWalletRepository,
                crystalLogRepository,
                askChatWalletRepository,
                askChatWalletLogRepository
        );
    }

    @Test
    void chargeTurns_consumes_crystals_and_charges_paid_turns() {
        User user = user(1L);
        AskChatWallet askChatWallet = askChatWallet(user, 5L, 2, 10);
        UserWallet crystalWallet = UserWallet.create(user, 70L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userWalletRepository.tryConsume(1L, 200L)).thenReturn(1);
        when(askChatWalletRepository.chargePaidTurns(1L, 10)).thenReturn(1);
        when(userWalletRepository.findByUserId(1L)).thenReturn(Optional.of(crystalWallet));
        when(askChatWalletRepository.findByUserId(1L)).thenReturn(Optional.of(askChatWallet));
        when(crystalLogRepository.save(any(CrystalLog.class))).thenAnswer(invocation -> {
            CrystalLog log = invocation.getArgument(0);
            ReflectionTestUtils.setField(log, "id", 9L);
            return log;
        });

        var response = service.chargeTurns(1L);

        assertThat(response.chargedTurnCount()).isEqualTo(10);
        assertThat(response.crystalCost()).isEqualTo(200L);
        assertThat(response.crystalBalance()).isEqualTo(70L);
        assertThat(response.freeTurnBalance()).isEqualTo(2);
        assertThat(response.paidTurnBalance()).isEqualTo(10);
        assertThat(response.totalTurnBalance()).isEqualTo(12);

        ArgumentCaptor<CrystalLog> crystalLogCaptor = ArgumentCaptor.forClass(CrystalLog.class);
        verify(crystalLogRepository).save(crystalLogCaptor.capture());
        assertThat(crystalLogCaptor.getValue().getDelta()).isEqualTo(-200L);
        assertThat(crystalLogCaptor.getValue().getBalanceAfter()).isEqualTo(70L);
        assertThat(crystalLogCaptor.getValue().getReason()).isEqualTo(CrystalLogReason.ASK_CHAT_TURN_CHARGE);
        assertThat(crystalLogCaptor.getValue().getStatus()).isEqualTo(CrystalLogStatus.CONFIRMED);
        assertThat(crystalLogCaptor.getValue().getRefType()).isEqualTo("ASK_CHAT_WALLET");
        assertThat(crystalLogCaptor.getValue().getRefId()).isEqualTo(5L);

        ArgumentCaptor<AskChatWalletLog> walletLogCaptor = ArgumentCaptor.forClass(AskChatWalletLog.class);
        verify(askChatWalletLogRepository).save(walletLogCaptor.capture());
        assertThat(walletLogCaptor.getValue().getFreeTurnDelta()).isZero();
        assertThat(walletLogCaptor.getValue().getPaidTurnDelta()).isEqualTo(10);
        assertThat(walletLogCaptor.getValue().getFreeTurnBalanceAfter()).isEqualTo(2);
        assertThat(walletLogCaptor.getValue().getPaidTurnBalanceAfter()).isEqualTo(10);
        assertThat(walletLogCaptor.getValue().getReason()).isEqualTo(AskChatWalletLogReason.CRYSTAL_CHARGE);
        assertThat(walletLogCaptor.getValue().getStatus()).isEqualTo(AskChatWalletLogStatus.CONFIRMED);
        assertThat(walletLogCaptor.getValue().getRefType()).isEqualTo("CRYSTAL_LOG");
        assertThat(walletLogCaptor.getValue().getRefId()).isEqualTo(9L);
        assertThat(walletLogCaptor.getValue().getIdempotencyKey()).isEqualTo("ask-chat-crystal-charge-9");
    }

    @Test
    void chargeTurns_rejects_when_crystal_balance_is_insufficient() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userWalletRepository.tryConsume(1L, 200L)).thenReturn(0);

        assertThatThrownBy(() -> service.chargeTurns(1L))
                .isInstanceOf(NotEnoughCrystalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_INSUFFICIENT_BALANCE);

        verify(askChatWalletRepository, never()).chargePaidTurns(any(), anyInt());
        verify(crystalLogRepository, never()).save(any());
        verify(askChatWalletLogRepository, never()).save(any());
    }

    @Test
    void chargeTurns_rejects_when_ask_chat_wallet_is_missing() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userWalletRepository.tryConsume(1L, 200L)).thenReturn(1);
        when(askChatWalletRepository.chargePaidTurns(1L, 10)).thenReturn(0);

        assertThatThrownBy(() -> service.chargeTurns(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASK_CHAT_WALLET_NOT_FOUND);

        verify(crystalLogRepository, never()).save(any());
        verify(askChatWalletLogRepository, never()).save(any());
    }

    private User user(Long id) {
        User user = User.createUser("test" + id + "@test.com", "hashed");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private AskChatWallet askChatWallet(User user, Long id, int freeTurnBalance, int paidTurnBalance) {
        AskChatWallet wallet = AskChatWallet.create(user, freeTurnBalance, paidTurnBalance);
        ReflectionTestUtils.setField(wallet, "id", id);
        return wallet;
    }
}
