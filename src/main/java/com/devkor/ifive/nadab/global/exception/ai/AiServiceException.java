package com.devkor.ifive.nadab.global.exception.ai;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BusinessException;

public class AiServiceException extends BusinessException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
}

