package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatTurnChargeResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AskChatWalletChargeService {

    public static final long ASK_CHAT_TURN_CHARGE_CRYSTAL_COST = 200L;
    public static final int ASK_CHAT_TURN_CHARGE_COUNT = 10;

    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;
    private final AskChatWalletRepository askChatWalletRepository;
    private final AskChatWalletLogRepository askChatWalletLogRepository;

    public AskChatTurnChargeResponse chargeTurns(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        int crystalUpdated = userWalletRepository.tryConsume(userId, ASK_CHAT_TURN_CHARGE_CRYSTAL_COST);
        if (crystalUpdated == 0) {
            throw new NotEnoughCrystalException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
        }

        int turnUpdated = askChatWalletRepository.chargePaidTurns(userId, ASK_CHAT_TURN_CHARGE_COUNT);
        if (turnUpdated == 0) {
            throw new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND);
        }

        UserWallet crystalWallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        AskChatWallet askChatWallet = askChatWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));

        CrystalLog crystalLog = crystalLogRepository.save(CrystalLog.createConfirmed(
                user,
                -ASK_CHAT_TURN_CHARGE_CRYSTAL_COST,
                crystalWallet.getCrystalBalance(),
                CrystalLogReason.ASK_CHAT_TURN_CHARGE,
                "ASK_CHAT_WALLET",
                askChatWallet.getId()
        ));

        askChatWalletLogRepository.save(AskChatWalletLog.createConfirmed(
                user,
                null,
                null,
                0,
                ASK_CHAT_TURN_CHARGE_COUNT,
                askChatWallet.getFreeTurnBalance(),
                askChatWallet.getPaidTurnBalance(),
                AskChatWalletLogReason.CRYSTAL_CHARGE,
                "CRYSTAL_LOG",
                crystalLog.getId(),
                "ask-chat-crystal-charge-" + crystalLog.getId()
        ));

        return AskChatTurnChargeResponse.of(
                crystalWallet.getCrystalBalance(),
                askChatWallet
        );
    }
}
