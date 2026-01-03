package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.core.entity.ProviderType;
import com.devkor.ifive.nadab.domain.auth.core.entity.SocialAccount;
import com.devkor.ifive.nadab.domain.auth.core.repository.SocialAccountRepository;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2UserInfo;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.GoogleOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.NaverOAuth2Client;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.state.StateManager;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.OAuth2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * OAuth2 소셜 로그인 서비스
 * - Authorization URL 생성
 * - OAuth2 로그인 처리 (사용자 조회/생성 + 토큰 발급)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthService {

    private final NaverOAuth2Client naverOAuth2Client;
    private final GoogleOAuth2Client googleOAuth2Client;
    private final StateManager stateManager;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserWalletRepository userWalletRepository;
    private final TokenService tokenService;

    // 프론트엔드에 전달할 Authorization URL 반환 (CSRF 방지를 위한 state 파라미터 포함)
    public String getAuthorizationUrl(OAuth2Provider provider) {
        // state 생성 및 저장
        String state = stateManager.generateAndStore();

        // provider에 따라 Authorization URL 생성
        return switch (provider) {
            case NAVER -> naverOAuth2Client.buildAuthorizationUrl(state);
            case GOOGLE -> googleOAuth2Client.buildAuthorizationUrl(state);
        };
    }

    // OAuth2 로그인 처리(Authorization Code → Access Token → 사용자 정보 → 토큰 발급)
    @Transactional
    public TokenBundle executeOAuth2Login(OAuth2Provider provider, String code, String state) {
        // 1. State 검증
        if (!stateManager.validateAndRemove(state)) {
            log.warn("OAuth2 state 검증 실패");
            throw new OAuth2Exception(ErrorCode.AUTH_INVALID_STATE);
        }

        // 2. Access Token 발급
        String accessToken = switch (provider) {
            case NAVER -> naverOAuth2Client.fetchAccessToken(code, state);
            case GOOGLE -> googleOAuth2Client.fetchAccessToken(code);
        };

        // 3. 사용자 정보 조회
        OAuth2UserInfo userInfo = switch (provider) {
            case NAVER -> naverOAuth2Client.fetchUserInfo(accessToken);
            case GOOGLE -> googleOAuth2Client.fetchUserInfo(accessToken);
        };

        // 4. 사용자 조회 또는 생성
        User user = getOrCreateUser(provider, userInfo.providerId(), userInfo.email());

        // 5. 토큰 발급 (Access Token + Refresh Token)
        return tokenService.issueTokens(user.getId());
    }

    // User 조회 또는 생성
    private User getOrCreateUser(OAuth2Provider provider, String providerId, String email) {
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