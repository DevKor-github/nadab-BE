package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

/**
 * 범용적인 인증 실패 예외
 */
public class UnauthorizedException extends BusinessException {

    // 기존 방식 (모든 에러 응답 개선 후 제거)
    public UnauthorizedException(String message) {
        super(message);
    }

    // 새로운 방식 (ErrorCode 기반)
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
