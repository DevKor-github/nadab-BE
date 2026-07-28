package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatWalletGrantServiceTest {

    @Mock
    private AskChatWalletRepository askChatWalletRepository;

    @Mock
    private AskChatWalletLogRepository askChatWalletLogRepository;

    private AskChatWalletGrantService service;

    @BeforeEach
    void setUp() {
        service = new AskChatWalletGrantService(
                askChatWalletRepository,
                askChatWalletLogRepository
        );
    }

    @Test
    void grantInitialFreeTurns_creates_wallet_and_confirmed_log() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.grantInitialFreeTurns(user);

        ArgumentCaptor<AskChatWallet> walletCaptor = ArgumentCaptor.forClass(AskChatWallet.class);
        verify(askChatWalletRepository).save(walletCaptor.capture());
        assertThat(walletCaptor.getValue().getUser()).isSameAs(user);
        assertThat(walletCaptor.getValue().getFreeTurnBalance()).isEqualTo(3);
        assertThat(walletCaptor.getValue().getPaidTurnBalance()).isZero();

        ArgumentCaptor<AskChatWalletLog> logCaptor = ArgumentCaptor.forClass(AskChatWalletLog.class);
        verify(askChatWalletLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getUser()).isSameAs(user);
        assertThat(logCaptor.getValue().getFreeTurnDelta()).isEqualTo(3);
        assertThat(logCaptor.getValue().getPaidTurnDelta()).isZero();
        assertThat(logCaptor.getValue().getFreeTurnBalanceAfter()).isEqualTo(3);
        assertThat(logCaptor.getValue().getPaidTurnBalanceAfter()).isZero();
        assertThat(logCaptor.getValue().getReason()).isEqualTo(AskChatWalletLogReason.INITIAL_FREE_GRANT);
        assertThat(logCaptor.getValue().getStatus()).isEqualTo(AskChatWalletLogStatus.CONFIRMED);
        assertThat(logCaptor.getValue().getRefType()).isEqualTo("SIGNUP");
        assertThat(logCaptor.getValue().getRefId()).isEqualTo(1L);
        assertThat(logCaptor.getValue().getIdempotencyKey()).isEqualTo("ask-chat-initial-free-1");
    }

    @Test
    void grantInitialFreeTurns_skips_when_wallet_already_exists() {
        User user = user(1L);
        when(askChatWalletRepository.findByUserId(1L))
                .thenReturn(Optional.of(AskChatWallet.create(user, 3, 0)));

        service.grantInitialFreeTurns(user);

        verify(askChatWalletRepository, never()).save(any());
        verify(askChatWalletLogRepository, never()).save(any());
    }

    private User user(Long id) {
        User user = User.createUser("test" + id + "@test.com", "hashed");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
