package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.core.entity.ProviderType;
import com.devkor.ifive.nadab.domain.auth.core.repository.SocialAccountRepository;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.client.AppleOAuth2Client;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import com.devkor.ifive.nadab.global.security.crypto.DataCryptoService;
import com.devkor.ifive.nadab.global.security.crypto.EncryptedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawalService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AppleOAuth2Client appleOAuth2Client;
    private final DataCryptoService dataCryptoService;

    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 이미 탈퇴한 경우
        if (user.getDeletedAt() != null) {
            throw new BadRequestException(ErrorCode.AUTH_ALREADY_WITHDRAWN);
        }

        // 애플 Refresh Token revoke
        revokeAppleTokenIfNeeded(user);

        // Soft Delete
        user.softDelete();
        user.updateSignupStatus(SignupStatusType.WITHDRAWN);

        // 모든 Refresh Token 삭제
        tokenService.revokeTokens(userId);
    }

    // 애플 계정 revoke
    private void revokeAppleTokenIfNeeded(User user) {
        socialAccountRepository.findByUser(user).ifPresent(account -> {
            if (account.getProviderType() == ProviderType.APPLE
                    && account.getRefreshToken() != null) {
                try {
                    // Refresh Token 복호화
                    EncryptedPayload payload = account.getRefreshToken().toPayload();
                    byte[] decrypted = dataCryptoService.decrypt(payload);
                    String refreshToken = new String(decrypted, UTF_8);

                    // 애플 서버에 revoke 요청
                    appleOAuth2Client.revokeToken(refreshToken);
                    log.info("애플 Refresh Token revoke 성공 - userId: {}", user.getId());
                } catch (Exception e) {
                    // Revoke 실패해도 탈퇴는 계속 진행
                    log.warn("애플 Refresh Token revoke 실패 (무시하고 계속) - userId: {}", user.getId(), e);
                }
            }
        });
    }

    // 회원 복구 (일반 유저)
    public TokenBundle restoreUser(String email, String password) {
        // 1. User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 2. 탈퇴하지 않은 계정
        if (user.getDeletedAt() == null) {
            throw new BadRequestException(ErrorCode.AUTH_NOT_WITHDRAWN);
        }

        // 3. 소셜 로그인 계정 차단
        if (socialAccountRepository.existsByUser(user)) {
            throw new BadRequestException(ErrorCode.AUTH_SOCIAL_ACCOUNT_RESTORE_FORBIDDEN);
        }

        // 4. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_PASSWORD);
        }

        // 5. 14일 이내인지 확인
        if (user.getDeletedAt().isBefore(OffsetDateTime.now().minusDays(14))) {
            throw new BadRequestException(ErrorCode.AUTH_RESTORE_PERIOD_EXPIRED);
        }

        // 6. 복구 처리
        user.restoreAccount();
        user.updateSignupStatus(SignupStatusType.COMPLETED);

        // 7. 토큰 발급
        return tokenService.issueTokens(user.getId());
    }

}