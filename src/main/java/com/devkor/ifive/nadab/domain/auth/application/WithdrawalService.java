package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.core.repository.SocialAccountRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawalService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 이미 탈퇴한 경우
        if (user.getDeletedAt() != null) {
            throw new BadRequestException(ErrorCode.AUTH_ALREADY_WITHDRAWN);
        }

        // Soft Delete
        user.softDelete();
        user.updateSignupStatus(SignupStatusType.WITHDRAWN);

        // 모든 Refresh Token 삭제
        tokenService.revokeTokens(userId);
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