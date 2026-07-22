package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

/**
 * 요청 빈도 제한(429 Too Many Requests) 초과 예외.
 * ErrorCode의 HttpStatus(TOO_MANY_REQUESTS)로 응답 상태가 결정된다.
 */
public class TooManyRequestsException extends BusinessException {

    public TooManyRequestsException(ErrorCode errorCode) {
        super(errorCode);
    }
}