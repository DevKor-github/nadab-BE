package com.devkor.ifive.nadab.global.exception.ai;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

public class AiServiceUnavailableException extends AiServiceException {

    public AiServiceUnavailableException(ErrorCode errorCode) {
        super(errorCode);
    }
}
