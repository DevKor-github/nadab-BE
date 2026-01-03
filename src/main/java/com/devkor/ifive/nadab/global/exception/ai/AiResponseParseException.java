package com.devkor.ifive.nadab.global.exception.ai;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

public class AiResponseParseException extends AiServiceException {
    public AiResponseParseException(String message) {
        super(message);
    }

    public AiResponseParseException(ErrorCode errorCode) {
        super(errorCode);
    }
}
