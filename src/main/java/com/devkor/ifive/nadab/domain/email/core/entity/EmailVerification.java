package com.devkor.ifive.nadab.domain.email.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends CreatableEntity {

    // 인증 코드 만료 시간 (분)
    private static final long EXPIRATION_MINUTES = 3L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false)
    private VerificationType verificationType;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    // 이메일 인증 생성
    public static EmailVerification create(
            String email,
            VerificationType verificationType,
            String verificationCode
    ) {
        EmailVerification verification = new EmailVerification();
        verification.email = email;
        verification.verificationType = verificationType;
        verification.verificationCode = verificationCode;
        verification.isVerified = false;
        verification.expiresAt = OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        return verification;
    }

    // 인증 코드 검증 및 상태 변경
    public boolean verify(String code) {
        if (!verificationCode.equals(code)) {
            return false;
        }
        this.isVerified = true;
        return true;
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}