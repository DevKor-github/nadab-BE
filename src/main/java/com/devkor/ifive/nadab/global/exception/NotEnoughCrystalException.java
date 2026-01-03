package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;

public class NotEnoughCrystalException extends BadRequestException {
    public NotEnoughCrystalException(String message) {
        super(message);
    }

    public NotEnoughCrystalException(ErrorCode errorCode) {
        super(errorCode);
    }
}
