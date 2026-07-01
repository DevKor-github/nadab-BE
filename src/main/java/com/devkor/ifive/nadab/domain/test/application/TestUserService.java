package com.devkor.ifive.nadab.domain.test.application;

import com.devkor.ifive.nadab.domain.terms.application.TermsCommandService;
import com.devkor.ifive.nadab.domain.test.api.dto.request.CreateTestUserRequest;
import com.devkor.ifive.nadab.domain.test.api.dto.response.CreateTestUserResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.UserInterestService;
import com.devkor.ifive.nadab.domain.user.core.service.UserProfileUpdateService;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile({"local", "dev"})
@Service
@Transactional
@RequiredArgsConstructor
public class TestUserService {

    private static final long DEFAULT_CRYSTAL_BALANCE = 1_000L;

    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserProfileUpdateService userProfileUpdateService;
    private final UserInterestService userInterestService;
    private final TermsCommandService termsCommandService;
    private final PasswordEncoder passwordEncoder;

    @Value("${test.account.password:}")
    private String testUserPassword;

    public CreateTestUserResponse createTestUser(CreateTestUserRequest request) {
        if (testUserPassword == null || testUserPassword.isBlank()) {
            throw new BadRequestException(ErrorCode.TEST_USER_PASSWORD_NOT_CONFIGURED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(testUserPassword);
        User user = User.createUser(request.email(), passwordHash);
        userRepository.save(user);

        userProfileUpdateService.updateNickname(user, request.nickname());
        userInterestService.updateUserInterest(user, InterestCode.PREFERENCE);
        user.updateSignupStatus(SignupStatusType.COMPLETED);

        userWalletRepository.save(UserWallet.create(user, DEFAULT_CRYSTAL_BALANCE));
        termsCommandService.saveConsents(user.getId(), true, true, true, false);

        return new CreateTestUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getSignupStatus().name()
        );
    }
}
