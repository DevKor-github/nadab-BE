package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.email.core.entity.EmailVerification;
import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import com.devkor.ifive.nadab.domain.email.core.repository.EmailVerificationRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 관리 서비스
 * - 비밀번호 찾기
 * - 비밀번호 변경 (마이페이지)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    // 비밀번호 재설정 (이메일 인증 완료 후)
    public void resetPassword(String email, String newPassword) {
        // 1. 이메일 인증 완료 확인
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndVerificationType(email, VerificationType.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("이메일 인증을 먼저 완료해주세요"));

        if (!verification.getIsVerified()) {
            throw new BadRequestException("이메일 인증이 완료되지 않았습니다");
        }

        // 2. User 조회 (이메일 인증 단계에서 소셜 계정/탈퇴 계정은 이미 차단돼서 검증X)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        // 3. 이전 비밀번호 재사용 검증
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException("이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다");
        }

        // 4. 비밀번호 해싱 및 변경
        String passwordHash = passwordEncoder.encode(newPassword);
        user.updatePasswordHash(passwordHash);

        // 5. EmailVerification 삭제
        emailVerificationRepository.delete(verification);

        // 6. 모든 Refresh Token 삭제 (모든 기기 강제 로그아웃)
        tokenService.revokeTokens(user.getId());
    }

    // 비밀번호 변경 + 자동 토큰 재발급 (마이페이지)
    public TokenBundle changePassword(Long userId, String currentPassword, String newPassword) {
        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        // 2. 소셜 로그인 사용자 차단
        if (user.isSocialAccount()) {
            throw new BadRequestException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다");
        }

        // 3. 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("현재 비밀번호가 일치하지 않습니다");
        }

        // 4. 이전 비밀번호 재사용 검증
        if (currentPassword.equals(newPassword)) {
            throw new BadRequestException("이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다");
        }

        // 5. 새 비밀번호 해싱 및 변경
        String passwordHash = passwordEncoder.encode(newPassword);
        user.updatePasswordHash(passwordHash);

        // 6. 모든 Refresh Token 삭제 (모든 기기 강제 로그아웃)
        tokenService.revokeTokens(userId);

        // 7. 새 토큰 발급 (현재 기기는 로그인 유지하기 위해서)
        return tokenService.issueTokens(userId);
    }
}