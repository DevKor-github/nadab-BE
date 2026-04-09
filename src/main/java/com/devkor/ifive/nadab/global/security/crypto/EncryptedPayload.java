package com.devkor.ifive.nadab.global.security.crypto;

import java.util.HexFormat;

/**
 * AES-256-GCM 암호화 결과를 담는 불변 DTO
 *
 * @param ciphertext 암호문
 * @param encryptedDataKey KMS로 암호화된 데이터 키
 * @param iv Initialization Vector (12 bytes)
 * @param authTag 무결성 검증용 태그 (16 bytes)
 */
public record EncryptedPayload(
        byte[] ciphertext,
        byte[] encryptedDataKey,
        byte[] iv,
        byte[] authTag
) {

    @Override
    public String toString() {
        return "EncryptedPayload{" +
                "ciphertext=" + ciphertext.length + "B, " + HexFormat.of().formatHex(ciphertext) +
                ", encryptedDataKey=" + encryptedDataKey.length + "B, " + HexFormat.of().formatHex(encryptedDataKey) +
                ", iv=" + iv.length + "B, " + HexFormat.of().formatHex(iv) +
                ", authTag=" + authTag.length + "B, " + HexFormat.of().formatHex(authTag) +
                '}';
    }
}