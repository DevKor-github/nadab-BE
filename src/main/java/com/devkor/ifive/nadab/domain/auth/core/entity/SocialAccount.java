package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider_type", "provider_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SocialAccount extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private ProviderType providerType;

    @Embedded
    private SocialAuthRefreshToken refreshToken;

    public static SocialAccount create(User user, String providerUserId, ProviderType providerType) {
        SocialAccount socialAccount = new SocialAccount();
        socialAccount.user = user;
        socialAccount.providerUserId = providerUserId;
        socialAccount.providerType = providerType;
        return socialAccount;
    }

    public void updateRefreshToken(SocialAuthRefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }
}
