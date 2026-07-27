package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
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

@Entity
@Table(name = "ask_chat_wallet_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatWalletLog extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_wallet_logs_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "session_id",
            foreignKey = @ForeignKey(name = "fk_ask_chat_wallet_logs_session")
    )
    private AskChatSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "message_id",
            foreignKey = @ForeignKey(name = "fk_ask_chat_wallet_logs_message")
    )
    private AskChatMessage message;

    @Column(name = "free_turn_delta", nullable = false)
    private int freeTurnDelta;

    @Column(name = "paid_turn_delta", nullable = false)
    private int paidTurnDelta;

    @Column(name = "free_turn_balance_after", nullable = false)
    private int freeTurnBalanceAfter;

    @Column(name = "paid_turn_balance_after", nullable = false)
    private int paidTurnBalanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 64)
    private AskChatWalletLogReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AskChatWalletLogStatus status;

    @Column(name = "ref_type", length = 64)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    public static AskChatWalletLog createConfirmed(
            User user,
            AskChatSession session,
            AskChatMessage message,
            int freeTurnDelta,
            int paidTurnDelta,
            int freeTurnBalanceAfter,
            int paidTurnBalanceAfter,
            AskChatWalletLogReason reason,
            String refType,
            Long refId,
            String idempotencyKey
    ) {
        AskChatWalletLog log = create(
                user,
                session,
                message,
                freeTurnDelta,
                paidTurnDelta,
                freeTurnBalanceAfter,
                paidTurnBalanceAfter,
                reason,
                refType,
                refId,
                idempotencyKey
        );
        log.status = AskChatWalletLogStatus.CONFIRMED;
        return log;
    }

    public static AskChatWalletLog createPending(
            User user,
            AskChatSession session,
            AskChatMessage message,
            int freeTurnDelta,
            int paidTurnDelta,
            int freeTurnBalanceAfter,
            int paidTurnBalanceAfter,
            AskChatWalletLogReason reason,
            String refType,
            Long refId,
            String idempotencyKey
    ) {
        AskChatWalletLog log = create(
                user,
                session,
                message,
                freeTurnDelta,
                paidTurnDelta,
                freeTurnBalanceAfter,
                paidTurnBalanceAfter,
                reason,
                refType,
                refId,
                idempotencyKey
        );
        log.status = AskChatWalletLogStatus.PENDING;
        return log;
    }

    private static AskChatWalletLog create(
            User user,
            AskChatSession session,
            AskChatMessage message,
            int freeTurnDelta,
            int paidTurnDelta,
            int freeTurnBalanceAfter,
            int paidTurnBalanceAfter,
            AskChatWalletLogReason reason,
            String refType,
            Long refId,
            String idempotencyKey
    ) {
        AskChatWalletLog log = new AskChatWalletLog();
        log.user = user;
        log.session = session;
        log.message = message;
        log.freeTurnDelta = freeTurnDelta;
        log.paidTurnDelta = paidTurnDelta;
        log.freeTurnBalanceAfter = freeTurnBalanceAfter;
        log.paidTurnBalanceAfter = paidTurnBalanceAfter;
        log.reason = reason;
        log.refType = refType;
        log.refId = refId;
        log.idempotencyKey = idempotencyKey;
        return log;
    }
}
