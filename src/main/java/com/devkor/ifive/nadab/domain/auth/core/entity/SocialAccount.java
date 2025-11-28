package com.devkor.ifive.nadab.domain.auth.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.CreatableEntity;
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

    // @Convert(converter = KmsEncryptedStringConverter.class)
    // 추후 구현 예정. 구현될 때까지 이 필드는 null로 둘 것
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken = null;

    public static SocialAccount createSocialAccount(User user, String providerUserId, ProviderType providerType) {
        SocialAccount socialAccount = new SocialAccount();
        socialAccount.user = user;
        socialAccount.providerUserId = providerUserId;
        socialAccount.providerType = providerType;
        return socialAccount;
    }
}
