package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

/**
 * JWT (Access token) 검증 예외
 */
public class JwtAuthException extends UnauthorizedException {

    public JwtAuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}