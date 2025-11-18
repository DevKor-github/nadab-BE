package com.devkor.ifive.nadab.global.security.token;

import com.devkor.ifive.nadab.global.security.util.SecureRandomBytesGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Refresh Token 생성 및 해싱
 * - Random Opaque Token 방식 사용 (JWT 아님)
 * - SHA-256 해싱
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenProvider {

    private static final int TOKEN_LENGTH = 32; // 256비트

    private final SecureRandomBytesGenerator secureRandomBytesGenerator;

    // Refresh Token(Opaque Token) 쌍(raw/hashed) 생성
    public RefreshTokenPair generateRefreshTokenPair() {
        String raw = generateRawRefreshToken();
        String hashed = hash(raw);
        return new RefreshTokenPair(raw, hashed);
    }

    // raw 토큰 생성 (URL-safe Base64)
    private String generateRawRefreshToken() {
        byte[] randomBytes = secureRandomBytesGenerator.generate(TOKEN_LENGTH);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // 토큰을 SHA-256으로 해싱 (Base64 인코딩)
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    // Refresh Token(Opaque Token) 쌍 - raw: 클라이언트 전달, hashed: DB 저장
    public record RefreshTokenPair(String raw, String hashed) {}
}