package com.devkor.ifive.nadab.global.exception;

/**
 * 범용적인 인증 실패 예외
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
