package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.ProviderType;
import com.devkor.ifive.nadab.domain.auth.core.entity.SocialAccount;
import com.devkor.ifive.nadab.domain.auth.core.repository.SocialAccountRepository;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 소셜 계정 관리 서비스
 * - 모든 소셜 로그인(OAuth2 웹, Native SDK)에서 재사용하는 공통 로직
 * - 사용자 조회/생성, 탈퇴 계정 복구 등
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAccountService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserWalletRepository userWalletRepository;

    // 소셜 계정으로 User 조회 또는 생성
    @Transactional
    public User getOrCreateUser(OAuth2Provider provider, String providerId, String email) {
        ProviderType providerType = ProviderType.valueOf(provider.name());

        return socialAccountRepository.findByProviderTypeAndProviderUserId(providerType, providerId)
                .map(SocialAccount::getUser)
                .map(user -> {
                    // 탈퇴한 계정이면 자동 복구
                    if (user.getDeletedAt() != null) {
                        return restoreWithdrawnAccount(user);
                    }
                    return user;
                })
                .orElseGet(() -> saveNewSocialUser(email, provider, providerId));
    }

    // User 조회 실패시 신규 소셜 로그인 사용자 생성
    private User saveNewSocialUser(String email, OAuth2Provider provider, String providerId) {
        // 이메일 중복 체크 및 탈퇴 계정 확인
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getDeletedAt() != null) {
                throw new BadRequestException(ErrorCode.AUTH_WITHDRAWN_ACCOUNT_RESTORE_REQUIRED);
            }
            throw new ConflictException(ErrorCode.AUTH_EMAIL_ALREADY_REGISTERED_WITH_DIFFERENT_METHOD);
        });

        // User 생성 및 저장
        User newUser = User.createSocialUser(email);
        userRepository.save(newUser);

        // UserWallet 생성 및 저장
        UserWallet wallet = UserWallet.create(newUser);
        userWalletRepository.save(wallet);

        // SocialAccount 생성 및 저장
        ProviderType providerType = ProviderType.valueOf(provider.name());
        SocialAccount socialAccount = SocialAccount.create(newUser, providerId, providerType);
        socialAccountRepository.save(socialAccount);

        return newUser;
    }

    // 탈퇴한 소셜 계정 자동 복구
    private User restoreWithdrawnAccount(User user) {
        // 14일 이내인지 확인
        if (user.getDeletedAt().isBefore(OffsetDateTime.now().minusDays(14))) {
            throw new BadRequestException(ErrorCode.AUTH_RESTORE_PERIOD_EXPIRED);
        }

        // 소셜 로그인 계정이 아닌 경우 차단 (일반 계정은 비밀번호 확인 필요)
        if (!socialAccountRepository.existsByUser(user)) {
            throw new BadRequestException(ErrorCode.AUTH_WITHDRAWN_ACCOUNT_RESTORE_REQUIRED);
        }

        // 복구 처리
        user.restoreAccount();
        user.updateSignupStatus(SignupStatusType.COMPLETED);

        return user;
    }
}