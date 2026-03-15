package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.RefreshToken;
import com.devkor.ifive.nadab.domain.auth.core.repository.RefreshTokenRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.properties.TokenProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import com.devkor.ifive.nadab.global.security.token.AccessTokenProvider;
import com.devkor.ifive.nadab.global.security.token.RefreshTokenPair;
import com.devkor.ifive.nadab.global.security.token.RefreshTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 토큰 발급/재발급/삭제 통합 관리
 * - Access Token + Refresh Token 항상 함께 처리 (Rotation 방식)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProperties tokenProperties;

    // Access Token + Refresh Token + SignupStatus, Controller에서 accessToken과 signupStatus는 응답 바디로 전달하고 refreshToken은 HttpOnly 쿠키로 전달할 예정
    public record TokenBundle(String accessToken, String refreshToken, String signupStatus) {}

    // 토큰 첫 발급 (로그인 시)
    public TokenBundle issueTokens(Long userId) {
        // User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // Access Token 생성 (TODO: role 관련 코드는 추후 권한 관리 때 수정 예정)
        List<String> roles = List.of("USER");
        String accessToken = accessTokenProvider.generateToken(userId, roles);

        // Refresh Token 생성 및 저장
        RefreshTokenPair tokenPair = refreshTokenProvider.generateRefreshTokenPair();
        OffsetDateTime expiresAt = OffsetDateTime.now()
                .plusSeconds(tokenProperties.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.create(user, tokenPair.hashed(), expiresAt);
        refreshTokenRepository.save(refreshToken);

        return new TokenBundle(accessToken, tokenPair.raw(), user.getSignupStatus().name());
    }

    // 토큰 재발급 (Rotation 방식)
    public TokenBundle refreshTokens(String rawRefreshToken) {
        // Refresh Token 해싱 및 DB 조회
        String hashedToken = refreshTokenProvider.hash(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByHashedToken(hashedToken)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        // 사용 가능한지 검증 (만료, revoke 확인)
        if (!refreshToken.isUsable()) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        Long userId = refreshToken.getUser().getId();

        // 기존 Refresh Token 삭제 (Rotation)
        refreshTokenRepository.deleteByHashedToken(hashedToken);

        // 새로운 토큰 번들 발급 (issueTokens 재사용)
        return issueTokens(userId);
    }

    // 모든 토큰 삭제 (로그아웃/탈퇴)
    public void revokeTokens(Long userId) {
        // DB에서 해당 사용자의 모든 Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(userId);
    }
}