package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 예외의 공통 부모 클래스
 * - 모든 커스텀 예외는 이 클래스를 상속
 * - ErrorCode 기반 예외 처리
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    private ErrorCode errorCode;

    // 기존 방식 (모든 에러 응답 개선 후 제거)
    protected BusinessException(String message) {
        super(message);
    }

    // 새로운 방식 (ErrorCode 기반)
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}