package com.devkor.ifive.nadab.global.exception.ai;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BusinessException;

public class AiServiceException extends BusinessException {

    public AiServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AiServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

