package com.devkor.ifive.nadab.global.exception;

/**
 * JWT (Access token) 검증 예외
 */
public class JwtAuthException extends UnauthorizedException {
    public JwtAuthException(String message) {
        super(message);
    }
}