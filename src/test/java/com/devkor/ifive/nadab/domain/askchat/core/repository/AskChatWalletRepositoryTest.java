package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatWalletRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    AskChatWalletRepository askChatWalletRepository;

    @Autowired
    AskChatWalletLogRepository askChatWalletLogRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void save_and_find_wallet_by_user_id() {
        User user = new UserBuilder(em).build();
        AskChatWallet wallet = AskChatWallet.create(user, 3, 10);
        askChatWalletRepository.save(wallet);

        em.flush();
        em.clear();

        AskChatWallet found = askChatWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(found.getFreeTurnBalance()).isEqualTo(3);
        assertThat(found.getPaidTurnBalance()).isEqualTo(10);
        assertThat(found.getTotalTurnBalance()).isEqualTo(13);
    }

    @Test
    void charge_paid_turns() {
        User user = new UserBuilder(em).build();
        askChatWalletRepository.save(AskChatWallet.create(user, 3, 0));

        int updated = askChatWalletRepository.chargePaidTurns(user.getId(), 10);

        em.flush();
        em.clear();

        AskChatWallet found = askChatWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(found.getFreeTurnBalance()).isEqualTo(3);
        assertThat(found.getPaidTurnBalance()).isEqualTo(10);
        assertThat(found.getTotalTurnBalance()).isEqualTo(13);
    }

    @Test
    void save_and_find_wallet_logs_by_user_id_desc() {
        User user = new UserBuilder(em).build();
        AskChatSession session = AskChatSession.start(user);
        em.persist(session);
        AskChatMessage userMessage = AskChatMessage.createUserMessage(session, "question");
        em.persist(userMessage);
        askChatWalletLogRepository.save(AskChatWalletLog.createConfirmed(
                user,
                session,
                userMessage,
                3,
                0,
                3,
                0,
                AskChatWalletLogReason.INITIAL_FREE_GRANT,
                "SIGNUP",
                user.getId(),
                "initial-free-" + user.getId()
        ));
        askChatWalletLogRepository.save(AskChatWalletLog.createConfirmed(
                user,
                session,
                userMessage,
                -1,
                0,
                2,
                0,
                AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME,
                "ASK_CHAT_MESSAGE",
                userMessage.getId(),
                "message-" + userMessage.getId()
        ));

        em.flush();
        em.clear();

        List<AskChatWalletLog> logs = askChatWalletLogRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(logs).hasSize(2);
        assertThat(logs)
                .extracting(AskChatWalletLog::getStatus)
                .containsOnly(AskChatWalletLogStatus.CONFIRMED);
        assertThat(logs)
                .extracting(AskChatWalletLog::getReason)
                .containsExactly(
                        AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME,
                        AskChatWalletLogReason.INITIAL_FREE_GRANT
                );
    }
}
