package com.devkor.ifive.nadab.domain.ads.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ad_reward_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdRewardSession extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_key", nullable = false, length = 80)
    private String sessionKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature", nullable = false, length = 40)
    private AdRewardFeature feature;

    @Column(name = "reward_amount", nullable = false)
    private long rewardAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AdRewardSessionStatus status;

    @Column(name = "transaction_id", length = 120)
    private String transactionId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "rewarded_at")
    private OffsetDateTime rewardedAt;

    public static AdRewardSession create(User user, String sessionKey, AdRewardFeature feature,
                                         long rewardAmount, OffsetDateTime expiresAt) {
        AdRewardSession session = new AdRewardSession();
        session.user = user;
        session.sessionKey = sessionKey;
        session.feature = feature;
        session.rewardAmount = rewardAmount;
        session.status = AdRewardSessionStatus.PENDING;
        session.expiresAt = expiresAt;
        return session;
    }

    public boolean isPending() {
        return status == AdRewardSessionStatus.PENDING;
    }

    public boolean isRewarded() {
        return status == AdRewardSessionStatus.REWARDED;
    }

    public boolean isExpired(OffsetDateTime now) {
        return expiresAt.isBefore(now);
    }

    public void markRewarded(String transactionId) {
        this.status = AdRewardSessionStatus.REWARDED;
        this.transactionId = transactionId;
        this.rewardedAt = OffsetDateTime.now();
    }

    public void markExpired() {
        this.status = AdRewardSessionStatus.EXPIRED;
    }

    public Long getUserId() {
        return user.getId();
    }
}