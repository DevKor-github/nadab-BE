package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatSessionRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    private AskChatSessionRepository askChatSessionRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findHistoriesByUserIdAndMessageRole_returns_only_sessions_with_user_message() {
        User user = persistUser("history-user@test.com");
        AskChatSession emptySession = persistSession(user);
        AskChatSession assistantOnlySession = persistSession(user);
        persistMessage(AskChatMessage.createFailedAssistantMessage(
                assistantOnlySession,
                "답변 생성에 실패했습니다.",
                null,
                null,
                "TEST_ERROR"
        ));
        AskChatSession firstHistory = persistSession(user);
        persistMessage(AskChatMessage.createUserMessage(firstHistory, "나는 어떤 사람이야?"));
        AskChatSession secondHistory = persistSession(user);
        persistMessage(AskChatMessage.createUserMessage(secondHistory, "내 장점은 뭐야?"));
        em.flush();
        em.clear();

        List<AskChatSession> histories = askChatSessionRepository.findHistoriesByUserIdAndMessageRole(
                user.getId(),
                AskChatMessageRole.USER,
                PageRequest.of(0, 10)
        );
        long totalCount = askChatSessionRepository.countHistoriesByUserIdAndMessageRole(
                user.getId(),
                AskChatMessageRole.USER
        );

        assertThat(histories)
                .extracting(AskChatSession::getId)
                .containsExactly(secondHistory.getId(), firstHistory.getId());
        assertThat(histories)
                .extracting(AskChatSession::getId)
                .doesNotContain(emptySession.getId(), assistantOnlySession.getId());
        assertThat(totalCount).isEqualTo(2);
    }

    private User persistUser(String email) {
        User user = User.createUser(email, "hashed");
        user.updateNickname(email.substring(0, email.indexOf('@')));
        return em.persistAndFlush(user);
    }

    private AskChatSession persistSession(User user) {
        AskChatSession session = AskChatSession.start(user);
        return em.persistAndFlush(session);
    }

    private AskChatMessage persistMessage(AskChatMessage message) {
        return em.persistAndFlush(message);
    }
}
