package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatWalletLogRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    AskChatWalletLogRepository askChatWalletLogRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void find_all_for_admin_filters_by_nickname_and_email() {
        // given
        User matchingUser = user("alice@example.com", "alice");
        User sameNicknameUser = user("bob@example.com", "alice-other");
        User otherUser = user("carol@example.com", "carol");

        AskChatWalletLog matchingLog = askChatWalletLogRepository.save(
                confirmedLog(matchingUser, -1, 0, 2, 9, 301L, "ask-filter-301")
        );
        askChatWalletLogRepository.save(
                confirmedLog(sameNicknameUser, 0, 10, 3, 10, 302L, "ask-filter-302")
        );
        askChatWalletLogRepository.save(
                confirmedLog(otherUser, 3, 0, 3, 0, 303L, "ask-filter-303")
        );

        em.flush();
        em.clear();

        // when
        Page<AskChatWalletLog> logs = askChatWalletLogRepository.findAllForAdmin(
                "ali",
                "alice@example.com",
                latestFirstPage()
        );

        // then
        assertThat(logs.getTotalElements()).isEqualTo(1);
        assertThat(logs.getContent()).extracting(AskChatWalletLog::getRefId)
                .containsExactly(301L);
        assertThat(logs.getContent().get(0).getUser().getEmail())
                .isEqualTo("alice@example.com");
        assertThat(logs.getContent().get(0).getFreeTurnDelta()).isEqualTo(-1);
        assertThat(logs.getContent().get(0).getPaidTurnDelta()).isZero();
        assertThat(matchingLog.getId()).isNotNull();
    }

    @Test
    void find_all_for_admin_orders_latest_first() {
        // given
        User user = user("latest@example.com", "latest");
        AskChatWalletLog first = askChatWalletLogRepository.save(
                confirmedLog(user, 3, 0, 3, 0, 401L, "ask-order-401")
        );
        AskChatWalletLog second = askChatWalletLogRepository.save(
                confirmedLog(user, 0, -1, 3, 9, 402L, "ask-order-402")
        );

        em.flush();
        em.clear();

        // when
        Page<AskChatWalletLog> logs = askChatWalletLogRepository.findAllForAdmin(
                null,
                null,
                latestFirstPage()
        );

        // then
        assertThat(logs.getTotalElements()).isEqualTo(2);
        assertThat(logs.getContent())
                .isSortedAccordingTo(
                        Comparator.comparing(AskChatWalletLog::getCreatedAt)
                                .thenComparing(AskChatWalletLog::getId)
                                .reversed()
                );
        assertThat(logs.getContent()).extracting(AskChatWalletLog::getId)
                .containsExactly(second.getId(), first.getId());
    }

    private AskChatWalletLog confirmedLog(
            User user,
            int freeTurnDelta,
            int paidTurnDelta,
            int freeTurnBalanceAfter,
            int paidTurnBalanceAfter,
            Long refId,
            String idempotencyKey
    ) {
        return AskChatWalletLog.createConfirmed(
                user,
                null,
                null,
                freeTurnDelta,
                paidTurnDelta,
                freeTurnBalanceAfter,
                paidTurnBalanceAfter,
                AskChatWalletLogReason.ANSWER_SUCCESS_CONSUME,
                "ASK_CHAT_MESSAGE",
                refId,
                idempotencyKey
        );
    }

    private User user(String email, String nickname) {
        User user = User.createUser(email, "hashed_password");
        user.updateNickname(nickname);
        em.persist(user);
        return user;
    }

    private PageRequest latestFirstPage() {
        return PageRequest.of(
                0,
                20,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
    }
}
