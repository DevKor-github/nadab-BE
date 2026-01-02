package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

/**
 * JWT (Access token) 검증 예외
 */
public class JwtAuthException extends UnauthorizedException {

    // 기존 방식 (모든 에러 응답 개선 후 제거)
    public JwtAuthException(String message) {
        super(message);
    }

    // 새로운 방식 (ErrorCode 기반)
    public JwtAuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}