package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatHistoryQueryServiceTest {

    @Mock
    private AskChatSessionRepository askChatSessionRepository;

    @Mock
    private AskChatMessageRepository askChatMessageRepository;

    private AskChatHistoryQueryService service;

    @BeforeEach
    void setUp() {
        service = new AskChatHistoryQueryService(
                askChatSessionRepository,
                askChatMessageRepository
        );
    }

    @Test
    void getHistories_returns_page_metadata_and_session_card_summary() {
        OffsetDateTime activeCreatedAt = OffsetDateTime.of(2026, 7, 13, 10, 0, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime activeLastMessageAt = OffsetDateTime.of(2026, 7, 13, 10, 3, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime endedCreatedAt = OffsetDateTime.of(2026, 7, 12, 10, 0, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime endedLastMessageAt = OffsetDateTime.of(2026, 7, 12, 10, 22, 0, 0, ZoneOffset.ofHours(9));
        AskChatSession active = session(
                10L,
                AskChatSessionStatus.ACTIVE,
                2,
                activeCreatedAt,
                null
        );
        AskChatSession ended = session(
                9L,
                AskChatSessionStatus.ENDED,
                15,
                endedCreatedAt,
                OffsetDateTime.of(2026, 7, 12, 10, 20, 0, 0, ZoneOffset.ofHours(9))
        );
        when(askChatSessionRepository.findHistoriesByUserIdAndMessageRole(
                1L,
                AskChatMessageRole.USER,
                PageRequest.of(0, 2)
        ))
                .thenReturn(List.of(active, ended));
        when(askChatSessionRepository.countHistoriesByUserIdAndMessageRole(1L, AskChatMessageRole.USER))
                .thenReturn(3L);
        AskChatMessage activeTitleMessage = message(
                100L,
                AskChatMessageRole.USER,
                "나는 어떤 사람이야?",
                activeCreatedAt
        );
        AskChatMessage activeLastUserMessage = message(
                102L,
                AskChatMessageRole.USER,
                "요즘 내가 놓치고 있는 감정은 뭐야?",
                OffsetDateTime.of(2026, 7, 13, 10, 2, 0, 0, ZoneOffset.ofHours(9))
        );
        AskChatMessage activeLastMessage = message(
                103L,
                AskChatMessageRole.ASSISTANT,
                "최근에는 안정감을 더 찾는 것 같아요.",
                activeLastMessageAt
        );
        AskChatMessage endedTitleMessage = message(
                90L,
                AskChatMessageRole.USER,
                "내 장점은 뭐야?",
                endedCreatedAt
        );
        AskChatMessage endedLastMessage = message(
                91L,
                AskChatMessageRole.ASSISTANT,
                "꾸준함이 장점으로 보여요.",
                endedLastMessageAt
        );
        when(askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtAsc(10L, AskChatMessageRole.USER))
                .thenReturn(Optional.of(activeTitleMessage));
        when(askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtDesc(10L, AskChatMessageRole.USER))
                .thenReturn(Optional.of(activeLastUserMessage));
        when(askChatMessageRepository.findFirstBySessionIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(activeLastMessage));
        when(askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtAsc(9L, AskChatMessageRole.USER))
                .thenReturn(Optional.of(endedTitleMessage));
        when(askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtDesc(9L, AskChatMessageRole.USER))
                .thenReturn(Optional.of(endedTitleMessage));
        when(askChatMessageRepository.findFirstBySessionIdOrderByCreatedAtDesc(9L))
                .thenReturn(Optional.of(endedLastMessage));

        var response = service.getHistories(1L, 1, 2);

        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.currentPage()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasPrevious()).isFalse();
        assertThat(response.hasNext()).isTrue();
        assertThat(response.histories())
                .extracting(
                        "sessionId",
                        "title",
                        "lastUserQuestion",
                        "createdDate",
                        "status",
                        "lastMessageAt"
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                10L,
                                "나는 어떤 사람이야?",
                                "요즘 내가 놓치고 있는 감정은 뭐야?",
                                activeCreatedAt.toLocalDate(),
                                AskChatSessionStatus.ACTIVE,
                                activeLastMessageAt
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                9L,
                                "내 장점은 뭐야?",
                                "내 장점은 뭐야?",
                                endedCreatedAt.toLocalDate(),
                                AskChatSessionStatus.ENDED,
                                endedLastMessageAt
                        )
                );
    }

    @Test
    void getHistories_returns_empty_response_when_no_user_message_session_exists() {
        when(askChatSessionRepository.findHistoriesByUserIdAndMessageRole(
                1L,
                AskChatMessageRole.USER,
                PageRequest.of(0, 20)
        )).thenReturn(List.of());
        when(askChatSessionRepository.countHistoriesByUserIdAndMessageRole(1L, AskChatMessageRole.USER))
                .thenReturn(0L);

        var response = service.getHistories(1L, 1, 20);

        assertThat(response.histories()).isEmpty();
        assertThat(response.totalCount()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.hasPrevious()).isFalse();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void getHistories_rejects_invalid_page_request() {
        assertThatThrownBy(() -> service.getHistories(1L, 0, 20))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.getHistories(1L, 1, 0))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.getHistories(1L, 1, 51))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getHistoryDetail_returns_session_messages_in_created_order() {
        AskChatSession session = session(
                10L,
                AskChatSessionStatus.ENDED,
                1,
                OffsetDateTime.of(2026, 7, 13, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2026, 7, 13, 10, 2, 0, 0, ZoneOffset.ofHours(9))
        );
        AskChatMessage userMessage = message(
                100L,
                AskChatMessageRole.USER,
                "나는 어떤 사람이야?",
                OffsetDateTime.of(2026, 7, 13, 10, 0, 0, 0, ZoneOffset.ofHours(9))
        );
        AskChatMessage assistantMessage = message(
                101L,
                AskChatMessageRole.ASSISTANT,
                "따뜻함을 자주 발견하는 사람이에요.",
                OffsetDateTime.of(2026, 7, 13, 10, 1, 0, 0, ZoneOffset.ofHours(9))
        );
        when(askChatSessionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(session));
        when(askChatMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(userMessage, assistantMessage));

        var response = service.getHistoryDetail(1L, 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(AskChatSessionStatus.ENDED);
        assertThat(response.answeredTurnCount()).isEqualTo(1);
        assertThat(response.readOnly()).isTrue();
        assertThat(response.messages())
                .extracting("id", "role", "content")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(100L, AskChatMessageRole.USER, "나는 어떤 사람이야?"),
                        org.assertj.core.groups.Tuple.tuple(101L, AskChatMessageRole.ASSISTANT, "따뜻함을 자주 발견하는 사람이에요.")
                );
    }

    @Test
    void getHistoryDetail_rejects_missing_or_other_user_session() {
        when(askChatSessionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHistoryDetail(1L, 10L))
                .isInstanceOfSatisfying(NotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ASK_CHAT_SESSION_NOT_FOUND));
    }

    private AskChatSession session(
            Long id,
            AskChatSessionStatus status,
            int answeredTurnCount,
            OffsetDateTime createdAt,
            OffsetDateTime endedAt
    ) {
        AskChatSession session = mock(AskChatSession.class);
        lenient().when(session.getId()).thenReturn(id);
        lenient().when(session.getStatus()).thenReturn(status);
        lenient().when(session.getAnsweredTurnCount()).thenReturn(answeredTurnCount);
        lenient().when(session.getCreatedAt()).thenReturn(createdAt);
        lenient().when(session.getEndedAt()).thenReturn(endedAt);
        return session;
    }

    private AskChatMessage message(
            Long id,
            AskChatMessageRole role,
            String content,
            OffsetDateTime createdAt
    ) {
        AskChatMessage message = mock(AskChatMessage.class);
        lenient().when(message.getId()).thenReturn(id);
        lenient().when(message.getRole()).thenReturn(role);
        lenient().when(message.getStatus()).thenReturn(AskChatMessageStatus.COMPLETED);
        lenient().when(message.getContent()).thenReturn(content);
        lenient().when(message.getCreatedAt()).thenReturn(createdAt);
        return message;
    }
}
