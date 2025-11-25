package com.devkor.ifive.nadab.global.security.token;

/**
 * Refresh Token 쌍 (raw + hashed)
 * - raw: 클라이언트에게 전달할 원본 토큰
 * - hashed: DB에 저장할 해시된 토큰
 */
public record RefreshTokenPair(String raw, String hashed) {
}