package com.devkor.ifive.nadab.domain.moderation.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "social_suspensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialSuspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    public static SocialSuspension create(User user, OffsetDateTime startedAt, OffsetDateTime expiresAt) {
        SocialSuspension s = new SocialSuspension();
        s.user = user;
        s.startedAt = startedAt;
        s.expiresAt = expiresAt;
        return s;
    }

}