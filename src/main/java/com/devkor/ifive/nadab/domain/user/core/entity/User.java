package com.devkor.ifive.nadab.domain.user.core.entity;

import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.global.shared.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
public class User extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_key")
    private String profileImageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_profile_type")
    private DefaultProfileType defaultProfileType;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_status")
    private SignupStatusType signupStatus;

    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;

    public static User createUser(String email) {
        User user = new User();
        user.email = email;
        user.signupStatus = SignupStatusType.PROFILE_INCOMPLETE;
        user.defaultProfileType = DefaultProfileType.DEFAULT;
        return user;
    }

    public static User createSocialUser(String email, OAuth2Provider provider, String providerId) {
        User user = new User();
        user.email = email;
        user.defaultProfileType = DefaultProfileType.DEFAULT;
        user.provider = provider.name();
        user.providerId = providerId;
        user.signupStatus = SignupStatusType.PROFILE_INCOMPLETE;
        user.passwordHash = null; // 소셜 로그인은 비밀번호 없음
        return user;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateSignupStatus(SignupStatusType status) {
        this.signupStatus = status;
    }

    public void updateToDefaultProfile(DefaultProfileType type) {
        this.profileImageKey = null;
        this.defaultProfileType = type;
    }

    public void updateToCustomProfile(String profileImageKey) {
        this.profileImageKey = profileImageKey;
        this.defaultProfileType = null;
    }

    /**
     * 탈퇴했던 유저를 재가입(계정 복구) 시키는 메소드
     */
    public void restoreAccount() {
        undoSoftDelete();
        this.registeredAt = OffsetDateTime.now();
    }

    /**
     * 초기 가입일 설정
     */
    @Override
    protected void postOnCreate() {
        this.registeredAt = this.createdAt;
    }
}
