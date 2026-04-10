package com.devkor.ifive.nadab.global.security.crypto;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 로컬 및 테스트 환경용 암호화 서비스
 *
 * XOR 기반 간단한 암호화로 실제 암호화 로직 테스트 가능
 */
@Component
@Profile({"local", "test"})
public class LocalDataCryptoService implements DataCryptoService {

    private static final byte[] FIXED_KEY = "nadab-local-test".getBytes(StandardCharsets.UTF_8);

    @Override
    public EncryptedPayload encrypt(byte[] plaintext) {
        byte[] ciphertext = xor(plaintext, FIXED_KEY);

        return new EncryptedPayload(
                ciphertext,
                new byte[0], // encryptedDataKey: 로컬에서는 빈 배열
                new byte[0], // iv: 로컬에서는 빈 배열
                new byte[0]  // authTag: 로컬에서는 빈 배열
        );
    }

    @Override
    public byte[] decrypt(EncryptedPayload payload) {
        return xor(payload.ciphertext(), FIXED_KEY);
    }

    // XOR 암호화/복호화 (동일한 키로 두 번 XOR하면 원본 복원)
    private byte[] xor(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }
}