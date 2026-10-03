package com.devkor.ifive.nadab.domain.ads.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ad_reward_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdRewardLog extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AdRewardSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false, length = 30)
    private AdRewardEvent event;

    @Column(name = "detail", length = 255)
    private String detail;

    @Column(name = "transaction_id", length = 120)
    private String transactionId;

    public static AdRewardLog create(User user, AdRewardSession session, AdRewardEvent event, String detail, String transactionId) {
        AdRewardLog log = new AdRewardLog();
        log.user = user;
        log.session = session;
        log.event = event;
        log.detail = detail;
        log.transactionId = transactionId;
        return log;
    }
}