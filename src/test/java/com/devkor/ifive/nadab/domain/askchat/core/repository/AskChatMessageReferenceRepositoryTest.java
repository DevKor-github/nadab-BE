package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageReference;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatMessageReferenceRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    private AskChatMessageReferenceRepository askChatMessageReferenceRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findAllByMessageIdOrderByDisplayOrderAsc_returns_references_in_prompt_order() {
        User user = persistUser("message-reference@test.com");
        AskChatSession session = em.persistAndFlush(AskChatSession.start(user));
        AskChatMessage assistantMessage = em.persistAndFlush(AskChatMessage.createAssistantMessage(
                session,
                "answer",
                null,
                null,
                null,
                null,
                null,
                null
        ));
        AskChatRagDocument firstDocument = persistRagDocument(user, 1L);
        AskChatRagDocument secondDocument = persistRagDocument(user, 2L);
        askChatMessageReferenceRepository.save(AskChatMessageReference.of(
                assistantMessage,
                secondDocument,
                2
        ));
        askChatMessageReferenceRepository.save(AskChatMessageReference.of(
                assistantMessage,
                firstDocument,
                1
        ));
        em.flush();
        em.clear();

        List<AskChatMessageReference> references =
                askChatMessageReferenceRepository.findAllByMessageIdOrderByDisplayOrderAsc(assistantMessage.getId());

        assertThat(references)
                .extracting(AskChatMessageReference::getDisplayOrder)
                .containsExactly(1, 2);
        assertThat(references)
                .extracting(reference -> reference.getRagDocument().getId())
                .containsExactly(firstDocument.getId(), secondDocument.getId());
    }

    private User persistUser(String email) {
        User user = User.createUser(email, "hashed");
        user.updateNickname(email.substring(0, email.indexOf('@')));
        return em.persistAndFlush(user);
    }

    private AskChatRagDocument persistRagDocument(User user, Long sourceId) {
        AskChatRagDocument document = AskChatRagDocument.createPending(
                user,
                AskChatRagDocumentSourceType.ANSWER_ENTRY,
                sourceId,
                InterestCode.RELATIONSHIP,
                "content " + sourceId,
                Map.of(),
                "text-embedding-3-small",
                1
        );
        document.markCompleted();
        return em.persistAndFlush(document);
    }
}
