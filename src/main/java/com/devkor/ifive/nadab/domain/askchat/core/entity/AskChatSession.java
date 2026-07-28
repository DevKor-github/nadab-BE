package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ask_chat_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatSession extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_sessions_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AskChatSessionStatus status;

    @Column(name = "answered_turn_count", nullable = false)
    private int answeredTurnCount;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    public static AskChatSession start(User user) {
        AskChatSession session = new AskChatSession();
        session.user = user;
        session.status = AskChatSessionStatus.ACTIVE;
        session.answeredTurnCount = 0;
        return session;
    }

    public void incrementAnsweredTurnCount() {
        this.answeredTurnCount++;
    }

    public void completeAnsweredTurn(int maxTurnCount) {
        incrementAnsweredTurnCount();
        if (this.answeredTurnCount >= maxTurnCount) {
            end();
        }
    }

    public void end() {
        if (this.status == AskChatSessionStatus.ENDED) {
            return;
        }
        this.status = AskChatSessionStatus.ENDED;
        this.endedAt = OffsetDateTime.now();
    }
}
