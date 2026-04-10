package com.devkor.ifive.nadab.global.security.crypto;

import com.devkor.ifive.nadab.global.security.util.SecureRandomBytesGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * AWS KMS를 사용한 Envelope Encryption 구현
 *
 * 동작 방식:
 * 1. KMS로 데이터 키(DEK) 생성
 * 2. DEK로 평문을 AES-256-GCM 암호화
 * 3. KMS가 암호화한 DEK와 함께 저장
 * 4. 복호화 시: KMS로 DEK 복호화 → DEK로 평문 복호화
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class KmsDataCryptoService implements DataCryptoService {

    private final KmsClient kmsClient;
    private final SecureRandomBytesGenerator randomGenerator;

    @Value("${cloud.aws.kms.key-id}")
    private String cmkArn;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    @Override
    public EncryptedPayload encrypt(byte[] plaintext) {
        try {
            // 1. KMS로 데이터 키 생성
            GenerateDataKeyResponse dataKeyResponse = kmsClient.generateDataKey(
                    GenerateDataKeyRequest.builder()
                            .keyId(cmkArn)
                            .keySpec(DataKeySpec.AES_256)
                            .build()
            );
            byte[] plaintextKey = dataKeyResponse.plaintext().asByteArray();
            byte[] encryptedDataKey = dataKeyResponse.ciphertextBlob().asByteArray();

            // 2. IV 생성
            byte[] iv = randomGenerator.generate(GCM_IV_LENGTH);

            // 3. AES-256-GCM으로 평문 암호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(plaintextKey, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] ciphertextWithTag = cipher.doFinal(plaintext);

            // 4. Ciphertext와 Tag 분리
            int tagLength = GCM_TAG_LENGTH / 8;
            byte[] ciphertext = Arrays.copyOfRange(ciphertextWithTag, 0, ciphertextWithTag.length - tagLength);
            byte[] tag = Arrays.copyOfRange(ciphertextWithTag, ciphertextWithTag.length - tagLength, ciphertextWithTag.length);

            return new EncryptedPayload(ciphertext, encryptedDataKey, iv, tag);

        } catch (Exception e) {
            log.error("데이터 암호화 실패", e);
            throw new RuntimeException("데이터 암호화에 실패했습니다", e);
        }
    }

    @Override
    public byte[] decrypt(EncryptedPayload payload) {
        try {
            // 1. KMS로 데이터 키 복호화
            byte[] plaintextKey = kmsClient.decrypt(r -> r
                            .ciphertextBlob(SdkBytes.fromByteArray(payload.encryptedDataKey())))
                    .plaintext()
                    .asByteArray();

            // 2. Ciphertext + Tag 결합
            byte[] ciphertextWithTag = ByteBuffer.allocate(payload.ciphertext().length + payload.authTag().length)
                    .put(payload.ciphertext())
                    .put(payload.authTag())
                    .array();

            // 3. AES-256-GCM으로 복호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(plaintextKey, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, payload.iv()));

            return cipher.doFinal(ciphertextWithTag);

        } catch (Exception e) {
            log.error("데이터 복호화 실패", e);
            throw new RuntimeException("데이터 복호화에 실패했습니다", e);
        }
    }
}