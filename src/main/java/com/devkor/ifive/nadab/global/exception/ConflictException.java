package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

public class ConflictException extends BusinessException {

    // 기존 방식 (모든 에러 응답 개선 후 제거)
    public ConflictException(String message) {
        super(message);
    }

    // 새로운 방식 (ErrorCode 기반)
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}