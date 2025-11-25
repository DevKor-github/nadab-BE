package com.devkor.ifive.nadab.domain.user.core.entity;

import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.global.shared.SoftDeletableAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
public class User extends SoftDeletableAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "email")
    private String email;

    @Setter
    @Column(name = "password_hash")
    private String passwordHash;

    @Setter
    @Column(name = "nickname")
    private String nickname;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_status")
    @Setter
    private SignupStatusType signupStatus;

    public static User createUser(String email) {
        User user = new User();
        user.email = email;
        user.signupStatus = SignupStatusType.PROFILE_INCOMPLETE;
        return user;
    }

    public static User createSocialUser(String email, OAuth2Provider provider, String providerId) {
        User user = new User();
        user.email = email;
        user.provider = provider.name();
        user.providerId = providerId;
        user.signupStatus = SignupStatusType.PROFILE_INCOMPLETE;
        user.passwordHash = null; // 소셜 로그인은 비밀번호 없음
        return user;
    }
}
