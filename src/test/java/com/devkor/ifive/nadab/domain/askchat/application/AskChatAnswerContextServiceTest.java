package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagSearchResultDto;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagVectorRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatAnswerContextServiceTest {

    @Mock
    private AskChatMessageRepository askChatMessageRepository;

    @Mock
    private AskChatEmbeddingClient askChatEmbeddingClient;

    @Mock
    private AskChatRagVectorRepository askChatRagVectorRepository;

    private AskChatAnswerContextService service;

    @BeforeEach
    void setUp() {
        AskChatAnswerProperties properties = new AskChatAnswerProperties();
        properties.setRecentMessageLimit(3);
        service = new AskChatAnswerContextService(
                askChatMessageRepository,
                askChatEmbeddingClient,
                askChatRagVectorRepository,
                properties
        );
    }

    @Test
    void build_collects_recent_completed_messages_and_rag_documents() {
        AskChatSession session = mock(AskChatSession.class);
        AskChatMessage latestAssistantMessage = AskChatMessage.createAssistantMessage(
                session,
                "꾸준함이 보여요.",
                null,
                null,
                null,
                null,
                null,
                null
        );
        AskChatMessage previousUserMessage = AskChatMessage.createUserMessage(session, "내 강점은 뭐야?");
        List<Double> queryEmbedding = List.of(0.1, 0.2, 0.3);
        AskChatRagSearchResultDto searchResult = new AskChatRagSearchResultDto(
                100L,
                AskChatRagDocumentSourceType.ANSWER_ENTRY,
                200L,
                InterestCode.VALUES,
                "사용자는 꾸준함과 책임감을 중요하게 말했다.",
                0.12
        );

        when(askChatMessageRepository.findAllBySessionIdAndStatusOrderByCreatedAtDesc(
                10L,
                com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus.COMPLETED,
                PageRequest.of(0, 3)
        )).thenReturn(List.of(latestAssistantMessage, previousUserMessage));
        when(askChatEmbeddingClient.embed("나는 어떤 사람에 가까워?")).thenReturn(queryEmbedding);
        when(askChatEmbeddingClient.retrievalLimit()).thenReturn(5);
        when(askChatRagVectorRepository.search(1L, null, queryEmbedding, 5))
                .thenReturn(List.of(searchResult));

        var context = service.build(1L, 10L, "나는 어떤 사람에 가까워?");

        assertThat(context.userId()).isEqualTo(1L);
        assertThat(context.sessionId()).isEqualTo(10L);
        assertThat(context.question()).isEqualTo("나는 어떤 사람에 가까워?");
        assertThat(context.recentMessages())
                .extracting(message -> message.role())
                .containsExactly(AskChatMessageRole.USER, AskChatMessageRole.ASSISTANT);
        assertThat(context.recentMessages())
                .extracting(message -> message.content())
                .containsExactly("내 강점은 뭐야?", "꾸준함이 보여요.");
        assertThat(context.referenceDocuments()).hasSize(1);
        assertThat(context.referenceDocuments().get(0).documentId()).isEqualTo(100L);
        assertThat(context.referenceDocuments().get(0).content())
                .isEqualTo("사용자는 꾸준함과 책임감을 중요하게 말했다.");
    }

    @Test
    void build_returns_empty_lists_when_recent_messages_and_rag_documents_do_not_exist() {
        when(askChatMessageRepository.findAllBySessionIdAndStatusOrderByCreatedAtDesc(
                10L,
                com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus.COMPLETED,
                PageRequest.of(0, 3)
        )).thenReturn(List.of());
        when(askChatEmbeddingClient.embed("기록이 부족해도 답해줘")).thenReturn(List.of(0.1, 0.2));
        when(askChatEmbeddingClient.retrievalLimit()).thenReturn(5);
        when(askChatRagVectorRepository.search(1L, null, List.of(0.1, 0.2), 5))
                .thenReturn(List.of());

        var context = service.build(1L, 10L, "기록이 부족해도 답해줘");

        assertThat(context.recentMessages()).isEmpty();
        assertThat(context.referenceDocuments()).isEmpty();
        verify(askChatEmbeddingClient).embed("기록이 부족해도 답해줘");
    }
}
