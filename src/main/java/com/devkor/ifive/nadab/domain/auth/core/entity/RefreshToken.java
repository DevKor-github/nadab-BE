package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hashed_token", nullable = false, unique = true)
    private String hashedToken;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public static RefreshToken create(User user, String hashedToken, OffsetDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.user = user;
        refreshToken.hashedToken = hashedToken;
        refreshToken.issuedAt = OffsetDateTime.now();
        refreshToken.expiresAt = expiresAt;
        refreshToken.revokedAt = null;
        return refreshToken;
    }

    private boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsable() {
        return revokedAt == null && !isExpired();
    }

    public void revoke() {
        if (revokedAt != null) {
            throw new IllegalStateException("이미 refresh token이 revoke되었습니다.");
        }
        revokedAt = OffsetDateTime.now();
    }
}