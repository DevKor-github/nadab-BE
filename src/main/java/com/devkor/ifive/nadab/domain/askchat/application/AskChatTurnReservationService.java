package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskChatTurnReservationService {

    private final AskChatWalletRepository askChatWalletRepository;

    public void ensureReservableTurn(Long userId) {
        AskChatWallet wallet = askChatWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));

        if (wallet.getTotalTurnBalance() <= 0) {
            throw new BadRequestException(ErrorCode.ASK_CHAT_TURN_BALANCE_INSUFFICIENT);
        }
    }
}
