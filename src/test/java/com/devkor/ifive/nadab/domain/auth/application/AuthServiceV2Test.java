package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.UserWithdrawalReason;
import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalReasonType;
import com.devkor.ifive.nadab.domain.auth.core.repository.UserWithdrawalReasonRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceV2Test {

    @Mock
    WithdrawalService withdrawalService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserWithdrawalReasonRepository userWithdrawalReasonRepository;

    AuthServiceV2 authServiceV2;

    @BeforeEach
    void setUp() {
        authServiceV2 = new AuthServiceV2(
                withdrawalService,
                userRepository,
                userWithdrawalReasonRepository
        );
    }

    @Test
    void withdrawUser_saves_selected_reasons_with_effective_withdrawn_at() {
        // given
        Long userId = 1L;
        User user = User.createUser("test@example.com", "hashed_password");
        doAnswer(invocation -> {
            user.softDelete();
            return null;
        }).when(withdrawalService).withdrawUser(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        // when
        authServiceV2.withdrawUser(
                userId,
                List.of(WithdrawalReasonType.DAILY_LOGGING_BURDEN, WithdrawalReasonType.OTHER),
                "  custom reason  "
        );

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserWithdrawalReason>> captor = ArgumentCaptor.forClass(List.class);
        verify(withdrawalService).withdrawUser(userId);
        verify(userWithdrawalReasonRepository).saveAll(captor.capture());

        List<UserWithdrawalReason> savedReasons = captor.getValue();
        OffsetDateTime deletedAt = user.getDeletedAt();

        assertThat(savedReasons).hasSize(2);
        assertThat(savedReasons)
                .extracting(UserWithdrawalReason::getUser)
                .containsOnly(user);
        assertThat(savedReasons)
                .extracting(UserWithdrawalReason::getWithdrawnAt)
                .containsOnly(deletedAt);
        assertThat(savedReasons)
                .extracting(UserWithdrawalReason::getReason)
                .containsExactly(
                        WithdrawalReasonType.DAILY_LOGGING_BURDEN,
                        WithdrawalReasonType.OTHER
                );
        assertThat(savedReasons.get(0).getCustomReason()).isNull();
        assertThat(savedReasons.get(1).getCustomReason()).isEqualTo("custom reason");
    }

    @Test
    void withdrawUser_rejects_empty_reasons_before_withdrawal() {
        assertValidationFailure(
                List.of(),
                null,
                ErrorCode.AUTH_WITHDRAWAL_REASON_REQUIRED
        );
    }

    @Test
    void withdrawUser_rejects_duplicated_reasons_before_withdrawal() {
        assertValidationFailure(
                List.of(WithdrawalReasonType.OTHER, WithdrawalReasonType.OTHER),
                "custom reason",
                ErrorCode.AUTH_WITHDRAWAL_REASON_DUPLICATED
        );
    }

    @Test
    void withdrawUser_rejects_other_without_custom_reason_before_withdrawal() {
        assertValidationFailure(
                List.of(WithdrawalReasonType.OTHER),
                "   ",
                ErrorCode.AUTH_WITHDRAWAL_OTHER_REASON_REQUIRED
        );
    }

    @Test
    void withdrawUser_rejects_custom_reason_without_other_before_withdrawal() {
        assertValidationFailure(
                List.of(WithdrawalReasonType.APP_ERROR_OR_SLOWNESS),
                "custom reason",
                ErrorCode.AUTH_WITHDRAWAL_OTHER_REASON_NOT_ALLOWED
        );
    }

    @Test
    void withdrawUser_rejects_too_long_custom_reason_before_withdrawal() {
        assertValidationFailure(
                List.of(WithdrawalReasonType.OTHER),
                "a".repeat(201),
                ErrorCode.AUTH_WITHDRAWAL_OTHER_REASON_TOO_LONG
        );
    }

    private void assertValidationFailure(
            List<WithdrawalReasonType> reasons,
            String customReason,
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(() -> authServiceV2.withdrawUser(1L, reasons, customReason))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(expectedErrorCode)
                );

        verify(withdrawalService, never()).withdrawUser(1L);
        verify(userRepository, never()).getReferenceById(1L);
        verify(userWithdrawalReasonRepository, never()).saveAll(anyList());
    }
}
