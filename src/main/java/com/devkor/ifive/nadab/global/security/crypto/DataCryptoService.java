package com.devkor.ifive.nadab.global.security.crypto;

/**
 * 민감 데이터 암호화/복호화 서비스 인터페이스
 *
 * 구현체:
 * - KmsDataCryptoService: AWS KMS를 사용한 Envelope Encryption (dev, prod)
 * - LocalDataCryptoService: XOR 기반 간단한 암호화 (local, test)
 */
public interface DataCryptoService {

    EncryptedPayload encrypt(byte[] plaintext);
    byte[] decrypt(EncryptedPayload payload);
}