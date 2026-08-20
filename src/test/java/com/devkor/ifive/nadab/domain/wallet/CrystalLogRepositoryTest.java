package com.devkor.ifive.nadab.domain.wallet;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
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
class CrystalLogRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    CrystalLogRepository crystalLogRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void find_all_for_admin_filters_by_nickname_and_email() {
        // given
        User matchingUser = user("alice@example.com", "alice");
        User sameNicknameUser = user("bob@example.com", "alice-other");
        User otherUser = user("carol@example.com", "carol");

        CrystalLog matchingLog = crystalLogRepository.save(
                confirmedLog(matchingUser, -100L, 900L, "MONTHLY_REPORT", 301L)
        );
        crystalLogRepository.save(
                confirmedLog(sameNicknameUser, -200L, 800L, "MONTHLY_REPORT", 302L)
        );
        crystalLogRepository.save(
                confirmedLog(otherUser, 100L, 1000L, "DAILY_REWARD", 303L)
        );

        em.flush();
        em.clear();

        // when
        Page<CrystalLog> logs = crystalLogRepository.findAllForAdmin(
                "ali",
                "alice@example.com",
                latestFirstPage()
        );

        // then
        assertThat(logs.getTotalElements()).isEqualTo(1);
        assertThat(logs.getContent()).extracting(CrystalLog::getRefId)
                .containsExactly(301L);
        assertThat(logs.getContent().get(0).getUser().getEmail())
                .isEqualTo("alice@example.com");
    }

    @Test
    void find_all_for_admin_orders_latest_first() {
        // given
        User user = user("latest@example.com", "latest");
        CrystalLog first = crystalLogRepository.save(
                confirmedLog(user, 100L, 100L, "DAILY_REWARD", 401L)
        );
        CrystalLog second = crystalLogRepository.save(
                crystalLog(user, -50L, 50L, CrystalLogStatus.PENDING, "MONTHLY_REPORT", 402L)
        );

        em.flush();
        em.clear();

        // when
        Page<CrystalLog> logs = crystalLogRepository.findAllForAdmin(
                null,
                null,
                latestFirstPage()
        );

        // then
        assertThat(logs.getTotalElements()).isEqualTo(2);
        assertThat(logs.getContent())
                .isSortedAccordingTo(
                        Comparator.comparing(CrystalLog::getCreatedAt)
                                .thenComparing(CrystalLog::getId)
                                .reversed()
                );
        assertThat(logs.getContent()).extracting(CrystalLog::getId)
                .containsExactly(second.getId(), first.getId());
    }

    private CrystalLog confirmedLog(User user, long delta, long balanceAfter, String refType, Long refId) {
        return crystalLog(user, delta, balanceAfter, CrystalLogStatus.CONFIRMED, refType, refId);
    }

    private CrystalLog crystalLog(
            User user,
            long delta,
            long balanceAfter,
            CrystalLogStatus status,
            String refType,
            Long refId
    ) {
        if (status == CrystalLogStatus.PENDING) {
            return CrystalLog.createPending(
                    user,
                    delta,
                    balanceAfter,
                    CrystalLogReason.REPORT_GENERATE_MONTHLY,
                    refType,
                    refId
            );
        }
        return CrystalLog.createConfirmed(
                user,
                delta,
                balanceAfter,
                CrystalLogReason.DAILY_ANSWER_REWARD,
                refType,
                refId
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
