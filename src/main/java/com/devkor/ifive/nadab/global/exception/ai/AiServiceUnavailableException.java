package com.devkor.ifive.nadab.global.exception.ai;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import lombok.Getter;

@Getter
public class AiServiceUnavailableException extends AiServiceException {

    private final Integer externalHttpStatus;
    private final String externalErrorCode;

    public AiServiceUnavailableException(ErrorCode errorCode) {
        super(errorCode);
        this.externalHttpStatus = null;
        this.externalErrorCode = null;
    }

    public AiServiceUnavailableException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.externalHttpStatus = null;
        this.externalErrorCode = null;
    }

    public AiServiceUnavailableException(
            ErrorCode errorCode,
            Integer externalHttpStatus,
            String externalErrorCode,
            Throwable cause
    ) {
        super(errorCode, cause);
        this.externalHttpStatus = externalHttpStatus;
        this.externalErrorCode = externalErrorCode;
    }
}
