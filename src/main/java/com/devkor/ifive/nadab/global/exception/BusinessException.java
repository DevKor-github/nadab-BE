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

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}