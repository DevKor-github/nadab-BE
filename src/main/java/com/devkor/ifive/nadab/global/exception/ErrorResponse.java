package com.devkor.ifive.nadab.global.exception;

public record ErrorResponse(
        int status,
        String message
) {
}
