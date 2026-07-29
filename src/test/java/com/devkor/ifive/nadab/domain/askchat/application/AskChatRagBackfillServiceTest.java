package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillResultDto;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillStatusDto;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillTargetDto;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagBackfillQueryRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatRagBackfillServiceTest {

    @Mock
    private AskChatRagBackfillQueryRepository backfillQueryRepository;

    @Mock
    private AskChatRagIndexingService indexingService;

    @Mock
    private AskChatEmbeddingClient embeddingClient;

    @Test
    void getCompletedDailyAnswerStatus_queries_current_embedding_version() {
        AskChatRagBackfillService service = new AskChatRagBackfillService(
                backfillQueryRepository,
                indexingService,
                embeddingClient
        );
        AskChatRagBackfillStatusDto expectedStatus = new AskChatRagBackfillStatusDto(3, 7, 1);
        when(embeddingClient.version()).thenReturn(2);
        when(backfillQueryRepository.findCompletedDailyAnswerStatus(2)).thenReturn(expectedStatus);

        AskChatRagBackfillStatusDto status = service.getCompletedDailyAnswerStatus();

        assertThat(status).isEqualTo(expectedStatus);
    }

    @Test
    void backfillCompletedDailyAnswers_indexes_targets_for_current_embedding_version() {
        AskChatRagBackfillService service = new AskChatRagBackfillService(
                backfillQueryRepository,
                indexingService,
                embeddingClient
        );
        when(embeddingClient.version()).thenReturn(2);
        when(embeddingClient.backfillBatchSize()).thenReturn(10);
        when(backfillQueryRepository.findCompletedDailyAnswerTargets(2, 10)).thenReturn(List.of(
                new AskChatRagBackfillTargetDto(10L, 100L, InterestCode.RELATIONSHIP),
                new AskChatRagBackfillTargetDto(11L, 101L, InterestCode.ROUTINE)
        ));

        AskChatRagBackfillResultDto result = service.backfillCompletedDailyAnswers();

        assertThat(result.targetCount()).isEqualTo(2);
        assertThat(result.indexedCount()).isEqualTo(2);
        assertThat(result.failedCount()).isZero();
        InOrder inOrder = inOrder(indexingService);
        inOrder.verify(indexingService).indexDailyAnswer(10L, 100L, InterestCode.RELATIONSHIP);
        inOrder.verify(indexingService).indexDailyAnswer(11L, 101L, InterestCode.ROUTINE);
    }

    @Test
    void backfillCompletedDailyAnswers_continues_when_target_fails() {
        AskChatRagBackfillService service = new AskChatRagBackfillService(
                backfillQueryRepository,
                indexingService,
                embeddingClient
        );
        when(embeddingClient.version()).thenReturn(1);
        when(embeddingClient.backfillBatchSize()).thenReturn(10);
        when(backfillQueryRepository.findCompletedDailyAnswerTargets(1, 10)).thenReturn(List.of(
                new AskChatRagBackfillTargetDto(10L, 100L, InterestCode.RELATIONSHIP),
                new AskChatRagBackfillTargetDto(11L, 101L, InterestCode.ROUTINE)
        ));
        doThrow(new IllegalStateException("failed"))
                .when(indexingService).indexDailyAnswer(10L, 100L, InterestCode.RELATIONSHIP);

        AskChatRagBackfillResultDto result = service.backfillCompletedDailyAnswers();

        assertThat(result.targetCount()).isEqualTo(2);
        assertThat(result.indexedCount()).isEqualTo(1);
        assertThat(result.failedCount()).isEqualTo(1);
        verify(indexingService).indexDailyAnswer(11L, 101L, InterestCode.ROUTINE);
    }
}
