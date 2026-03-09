package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

public class FcmException extends BusinessException {

    public FcmException(ErrorCode errorCode) {
        super(errorCode);
    }
}