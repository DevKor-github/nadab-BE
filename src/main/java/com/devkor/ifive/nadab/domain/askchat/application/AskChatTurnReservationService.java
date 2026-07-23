package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatTurnReservation;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AskChatTurnReservationService {

    private final AskChatWalletRepository askChatWalletRepository;
    private final AskChatWalletLogRepository askChatWalletLogRepository;

    public AskChatTurnReservation reserveTurn(Long userId, AskChatSession session) {
        AskChatWallet wallet = askChatWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));

        if (wallet.getTotalTurnBalance() <= 0) {
            throw new BadRequestException(ErrorCode.ASK_CHAT_TURN_BALANCE_INSUFFICIENT);
        }

        int freeTurnDelta = 0;
        int paidTurnDelta = 0;
        int updated = askChatWalletRepository.tryReserveFreeTurn(userId);
        if (updated == 1) {
            freeTurnDelta = -1;
        } else {
            updated = askChatWalletRepository.tryReservePaidTurn(userId);
            if (updated == 1) {
                paidTurnDelta = -1;
            }
        }

        if (updated == 0) {
            throw new BadRequestException(ErrorCode.ASK_CHAT_TURN_BALANCE_INSUFFICIENT);
        }

        AskChatWallet reservedWallet = askChatWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));
        AskChatWalletLog reservationLog = askChatWalletLogRepository.save(AskChatWalletLog.createPending(
                session.getUser(),
                session,
                null,
                freeTurnDelta,
                paidTurnDelta,
                reservedWallet.getFreeTurnBalance(),
                reservedWallet.getPaidTurnBalance(),
                AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME,
                "ASK_CHAT_SESSION",
                session.getId(),
                "ask-chat-turn-reserve-" + session.getId() + "-" + System.nanoTime()
        ));

        return new AskChatTurnReservation(
                reservationLog.getId(),
                freeTurnDelta,
                paidTurnDelta
        );
    }

    public void confirm(AskChatTurnReservation reservation) {
        int updated = askChatWalletLogRepository.markConfirmed(reservation.walletLogId());
        if (updated == 0) {
            throw new NotFoundException(ErrorCode.ASK_CHAT_WALLET_LOG_NOT_FOUND);
        }
    }

    public void refund(Long userId, AskChatSession session, AskChatTurnReservation reservation) {
        int updated;
        if (reservation.usedFreeTurn()) {
            updated = askChatWalletRepository.refundFreeTurn(userId);
        } else if (reservation.usedPaidTurn()) {
            updated = askChatWalletRepository.refundPaidTurn(userId);
        } else {
            return;
        }

        if (updated == 0) {
            throw new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND);
        }

        int logUpdated = askChatWalletLogRepository.markRefunded(reservation.walletLogId());
        if (logUpdated == 0) {
            throw new NotFoundException(ErrorCode.ASK_CHAT_WALLET_LOG_NOT_FOUND);
        }
        AskChatWallet refundedWallet = askChatWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));
        User user = session.getUser();
        askChatWalletLogRepository.save(AskChatWalletLog.createConfirmed(
                user,
                session,
                null,
                reservation.usedFreeTurn() ? 1 : 0,
                reservation.usedPaidTurn() ? 1 : 0,
                refundedWallet.getFreeTurnBalance(),
                refundedWallet.getPaidTurnBalance(),
                AskChatWalletLogReason.ANSWER_FAILURE_REFUND,
                "ASK_CHAT_WALLET_LOG",
                reservation.walletLogId(),
                "ask-chat-turn-refund-" + reservation.walletLogId()
        ));
    }
}
