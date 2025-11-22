package com.devkor.ifive.nadab.global.exception;

import lombok.Getter;

/**
 * OAuth2 인증 예외
 * - OAuth2 제공자(네이버, 구글)로 인한 예외
 */
@Getter
public class OAuth2Exception extends RuntimeException {
    private final int statusCode;

    public OAuth2Exception(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}