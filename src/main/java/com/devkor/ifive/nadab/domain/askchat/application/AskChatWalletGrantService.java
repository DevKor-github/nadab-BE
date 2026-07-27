package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AskChatWalletGrantService {

    public static final int INITIAL_FREE_TURN_COUNT = 3;
    private static final String INITIAL_FREE_REF_TYPE = "SIGNUP";
    private static final String INITIAL_FREE_IDEMPOTENCY_PREFIX = "ask-chat-initial-free-";

    private final AskChatWalletRepository askChatWalletRepository;
    private final AskChatWalletLogRepository askChatWalletLogRepository;

    @Transactional
    public void grantInitialFreeTurns(User user) {
        if (askChatWalletRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        AskChatWallet wallet = AskChatWallet.create(user, INITIAL_FREE_TURN_COUNT, 0);
        askChatWalletRepository.save(wallet);
        askChatWalletLogRepository.save(AskChatWalletLog.createConfirmed(
                user,
                null,
                null,
                INITIAL_FREE_TURN_COUNT,
                0,
                INITIAL_FREE_TURN_COUNT,
                0,
                AskChatWalletLogReason.INITIAL_FREE_GRANT,
                INITIAL_FREE_REF_TYPE,
                user.getId(),
                INITIAL_FREE_IDEMPOTENCY_PREFIX + user.getId()
        ));
    }
}
