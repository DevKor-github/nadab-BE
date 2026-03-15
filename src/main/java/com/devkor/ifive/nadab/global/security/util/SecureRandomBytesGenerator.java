package com.devkor.ifive.nadab.global.security.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 랜덤 바이트 배열 생성기
 */
@Component
public class SecureRandomBytesGenerator {

    private final SecureRandom secureRandom;

    public SecureRandomBytesGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public byte[] generate(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}