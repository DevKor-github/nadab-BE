package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminAskChatWalletLogResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAskChatWalletLogQueryServiceTest {

    @Mock
    private AskChatWalletLogRepository askChatWalletLogRepository;

    @Test
    void maps_ask_chat_wallet_logs_to_admin_page_response() {
        AdminAskChatWalletLogQueryService service =
                new AdminAskChatWalletLogQueryService(askChatWalletLogRepository);
        User user = User.createUser("alice@example.com", "hashed_password");
        user.updateNickname("alice");
        AskChatWalletLog log = AskChatWalletLog.createPending(
                user,
                null,
                null,
                0,
                -1,
                2,
                9,
                AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME,
                "ASK_CHAT_MESSAGE",
                501L,
                "ask-chat-message-501"
        );
        AdminLogSearchCondition condition = AdminLogSearchCondition.of(2, 10, "alice", null);
        when(askChatWalletLogRepository.findAllForAdmin(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(log), condition.toPageable(), 11));

        AdminLogPageResponse<AdminAskChatWalletLogResponse> response = service.getLogs(condition);

        assertThat(response.currentPage()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.totalCount()).isEqualTo(11);
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.user().nickname()).isEqualTo("alice");
            assertThat(item.user().email()).isEqualTo("alice@example.com");
            assertThat(item.sessionId()).isNull();
            assertThat(item.messageId()).isNull();
            assertThat(item.freeTurnDelta()).isZero();
            assertThat(item.paidTurnDelta()).isEqualTo(-1);
            assertThat(item.freeTurnBalanceAfter()).isEqualTo(2);
            assertThat(item.paidTurnBalanceAfter()).isEqualTo(9);
            assertThat(item.reason()).isEqualTo(AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME);
            assertThat(item.status()).isEqualTo(AskChatWalletLogStatus.PENDING);
            assertThat(item.refType()).isEqualTo("ASK_CHAT_MESSAGE");
            assertThat(item.refId()).isEqualTo(501L);
            assertThat(item.idempotencyKey()).isEqualTo("ask-chat-message-501");
        });

        ArgumentCaptor<String> nicknameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(askChatWalletLogRepository).findAllForAdmin(
                nicknameCaptor.capture(),
                emailCaptor.capture(),
                pageableCaptor.capture()
        );
        assertThat(nicknameCaptor.getValue()).isEqualTo("alice");
        assertThat(emailCaptor.getValue()).isNull();
        assertThat(pageableCaptor.getValue()).isEqualTo(condition.toPageable());
    }
}
