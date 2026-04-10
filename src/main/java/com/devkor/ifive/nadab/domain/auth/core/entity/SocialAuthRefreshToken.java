package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.global.security.crypto.EncryptedPayload;
import jakarta.persistence.*;
import lombok.*;

/**
 * KMS Envelope Encryption을 사용한 Refresh Token 저장 (불변 객체)
 * - ciphertext: AES-256-GCM으로 암호화된 Refresh Token
 * - key: KMS로 암호화된 데이터 키 (Data Encryption Key)
 * - iv: AES-GCM Initialization Vector
 * - tag: AES-GCM Authentication Tag
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAuthRefreshToken {

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_ciphertext", columnDefinition = "BYTEA")
    private byte[] ciphertext;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_key", columnDefinition = "BYTEA")
    private byte[] key;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_iv", columnDefinition = "BYTEA")
    private byte[] iv;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_tag", columnDefinition = "BYTEA")
    private byte[] tag;

    public static SocialAuthRefreshToken from(EncryptedPayload payload) {
        return new SocialAuthRefreshToken(
                payload.ciphertext(),
                payload.encryptedDataKey(),
                payload.iv(),
                payload.authTag()
        );
    }

    public EncryptedPayload toPayload() {
        return new EncryptedPayload(ciphertext, key, iv, tag);
    }
}