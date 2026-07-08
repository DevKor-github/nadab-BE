package com.devkor.ifive.nadab.domain.test.application;

import com.devkor.ifive.nadab.domain.terms.application.TermsCommandService;
import com.devkor.ifive.nadab.domain.test.api.dto.request.CreateTestUserRequest;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.UserInterestService;
import com.devkor.ifive.nadab.domain.user.core.service.UserProfileUpdateService;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserWalletRepository userWalletRepository;
    @Mock
    private UserProfileUpdateService userProfileUpdateService;
    @Mock
    private UserInterestService userInterestService;
    @Mock
    private TermsCommandService termsCommandService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private TestUserService testUserService;

    @BeforeEach
    void setUp() {
        testUserService = new TestUserService(
                userRepository,
                userWalletRepository,
                userProfileUpdateService,
                userInterestService,
                termsCommandService,
                passwordEncoder
        );
        ReflectionTestUtils.setField(testUserService, "testUserPassword", "test-password");
    }

    @Test
    void createTestUser_sets_terms_interest_and_default_crystals() {
        CreateTestUserRequest request = new CreateTestUserRequest(
                "test@example.com",
                "tester"
        );
        when(passwordEncoder.encode("test-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        var response = testUserService.createTestUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userInterestService).updateUserInterest(userCaptor.capture(), eq(InterestCode.PREFERENCE));
        verify(termsCommandService).saveConsents(1L, true, true, true, false);

        ArgumentCaptor<UserWallet> walletCaptor = ArgumentCaptor.forClass(UserWallet.class);
        verify(userWalletRepository).save(walletCaptor.capture());
        assertThat(walletCaptor.getValue().getCrystalBalance()).isEqualTo(1_000L);
        assertThat(userCaptor.getValue().getSignupStatus()).isEqualTo(SignupStatusType.COMPLETED);
        assertThat(response.signupStatus()).isEqualTo(SignupStatusType.COMPLETED.name());
    }
}
