package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalEvent;
import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalEventReason;
import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalReasonType;
import com.devkor.ifive.nadab.domain.auth.core.repository.SocialAccountRepository;
import com.devkor.ifive.nadab.domain.auth.core.repository.UserWithdrawalReasonRepository;
import com.devkor.ifive.nadab.domain.auth.core.repository.WithdrawalEventReasonRepository;
import com.devkor.ifive.nadab.domain.auth.core.repository.WithdrawalEventRepository;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.AppleOAuth2Client;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.security.crypto.DataCryptoService;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({AuthServiceV2.class, WithdrawalService.class})
class AuthServiceV2PersistenceTest extends PostgresIntegrationTestSupport {

    @MockitoBean
    SocialAccountRepository socialAccountRepository;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    AppleOAuth2Client appleOAuth2Client;

    @MockitoBean
    DataCryptoService dataCryptoService;

    @MockitoSpyBean
    WithdrawalEventReasonRepository withdrawalEventReasonRepository;

    @Autowired
    AuthServiceV2 authServiceV2;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserWithdrawalReasonRepository userWithdrawalReasonRepository;

    @Autowired
    WithdrawalEventRepository withdrawalEventRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Long userId;

    @AfterEach
    void tearDown() {
        if (userId == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM withdrawal_events WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_withdrawal_reasons WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }

    @Test
    void withdrawal_commits_user_state_and_new_reason_store_without_legacy_write() {
        userId = createUser();

        authServiceV2.withdrawUser(
                userId,
                List.of(WithdrawalReasonType.DAILY_LOGGING_BURDEN, WithdrawalReasonType.OTHER),
                "기타 의견"
        );

        User withdrawnUser = userRepository.findById(userId).orElseThrow();
        WithdrawalEvent event = withdrawalEventRepository.findAll().getFirst();
        List<WithdrawalEventReason> eventReasons = withdrawalEventReasonRepository.findAll();

        assertThat(withdrawnUser.getDeletedAt()).isNotNull();
        assertThat(withdrawnUser.getSignupStatus()).isEqualTo(SignupStatusType.WITHDRAWN);
        assertThat(event.getWithdrawnAt()).isEqualTo(withdrawnUser.getDeletedAt());
        assertThat(event.getExpiresAt()).isEqualTo(withdrawnUser.getDeletedAt().plusYears(1));
        assertThat(eventReasons)
                .extracting(WithdrawalEventReason::getReason)
                .containsExactlyInAnyOrder(
                        WithdrawalReasonType.DAILY_LOGGING_BURDEN,
                        WithdrawalReasonType.OTHER
                );
        assertThat(userWithdrawalReasonRepository.count()).isZero();
    }

    @Test
    void new_reason_store_failure_rolls_back_withdrawal_and_event() {
        userId = createUser();
        doThrow(new IllegalStateException("신규 탈퇴 사유 저장 실패"))
                .when(withdrawalEventReasonRepository).saveAll(anyList());

        assertThatThrownBy(() -> authServiceV2.withdrawUser(
                userId,
                List.of(WithdrawalReasonType.APP_ERROR_OR_SLOWNESS),
                null
        )).isInstanceOf(IllegalStateException.class);

        User activeUser = userRepository.findById(userId).orElseThrow();
        assertThat(activeUser.getDeletedAt()).isNull();
        assertThat(activeUser.getSignupStatus()).isEqualTo(SignupStatusType.PROFILE_INCOMPLETE);
        assertThat(withdrawalEventRepository.count()).isZero();
        assertThat(withdrawalEventReasonRepository.count()).isZero();
        assertThat(userWithdrawalReasonRepository.count()).isZero();
    }

    private Long createUser() {
        User user = User.createUser("withdrawal+" + System.nanoTime() + "@test.com", "hashed_password");
        user.updateNickname("wd" + System.nanoTime());
        return userRepository.save(user).getId();
    }
}
