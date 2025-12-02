package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.email.core.entity.EmailVerification;
import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import com.devkor.ifive.nadab.domain.email.core.repository.EmailVerificationRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일반 회원가입/로그인 서비스
 * - 회원가입: 이메일 인증 확인 → User 생성(PROFILE_INCOMPLETE) → 토큰 발급 → 이메일 인증 레코드 삭제
 * - 로그인: 이메일/비밀번호 검증 → 토큰 발급
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BasicAuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입(이메일 인증 확인, 중복 확인, User 생성, 토큰 발급, 이메일 인증 레코드 삭제)
    public TokenBundle signup(String email, String password) {
        // 1. 이메일 인증 완료 확인
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndVerificationType(email, VerificationType.SIGNUP)
                .orElseThrow(() -> new BadRequestException("이메일 인증을 먼저 완료해주세요"));

        if (!verification.getIsVerified()) {
            throw new BadRequestException("이메일 인증이 완료되지 않았습니다");
        }

        // 2. 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("이미 사용 중인 이메일입니다");
        }

        // 3. 비밀번호 해싱
        String passwordHash = passwordEncoder.encode(password);

        // 4. User 생성 (PROFILE_INCOMPLETE)
        User user = User.createUser(email, passwordHash);
        userRepository.save(user);

        // 5. 토큰 발급 (PROFILE_INCOMPLETE 상태)
        TokenBundle tokenBundle = tokenService.issueTokens(user.getId());

        // 6. 이메일 인증 레코드 삭제 (회원가입 완료, 정리 작업)
        emailVerificationRepository.delete(verification);

        return tokenBundle;
    }

    // 로그인(이메일로 User 조회, 비밀번호 검증, 토큰 발급)
    public TokenBundle login(String email, String password) {
        // 1. User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다");
        }

        // 3. 토큰 발급
        return tokenService.issueTokens(user.getId());
    }
}