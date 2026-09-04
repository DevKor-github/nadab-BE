package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagDocumentStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagReferenceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyWalletStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSourceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatWalletSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.repository.AskChatStatsRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AskChatStatsServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Test
    void getAskChatStats_fills_missing_days_and_calculates_quality_summary() {
        AskChatStatsRepository repository = mock(AskChatStatsRepository.class);
        AskChatStatsService service = new AskChatStatsService(repository);
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 12);
        OffsetDateTime startInclusive = startDate.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime endExclusive = endDate.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();

        when(repository.findDailySessionStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatDailySessionStatsDto(startDate, 2L, 1L, 1L, 1L),
                new AskChatDailySessionStatsDto(endDate, 1L, 1L, 1L, 0L)
        ));
        when(repository.findDailyMessageStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatDailyMessageStatsDto(startDate, 2L, 1L, 1L, 123.4, 200L),
                new AskChatDailyMessageStatsDto(endDate, 1L, 2L, 0L, 300.0, 300L)
        ));
        when(repository.findSessionSummary(startInclusive, endExclusive))
                .thenReturn(new AskChatSessionSummaryDto(3L, 2L, 2L, 1L));
        when(repository.findMessageSummary(startInclusive, endExclusive))
                .thenReturn(new AskChatMessageSummaryDto(3L, 3L, 1L, 200.0, 280L));
        when(repository.findAssistantErrorStats(startInclusive, endExclusive))
                .thenReturn(List.of(new AskChatErrorStatsDto("TIMEOUT", 1L)));
        when(repository.findDailyWalletStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatDailyWalletStatsDto(startDate, 5L, 1L, 3L, 1L, 7L, 2L, 1L, 1L, 0L, 4L, 5L, -1L),
                new AskChatDailyWalletStatsDto(endDate, 2L, 0L, 2L, 0L, 0L, 0L, 2L, 0L, 0L, 5L, 0L, -2L)
        ));
        when(repository.findWalletSummary(startInclusive, endExclusive))
                .thenReturn(new AskChatWalletSummaryDto(7L, 1L, 5L, 1L, 7L, 1L, 3L, 1L, 0L, 9L, 5L, -3L));
        when(repository.findDailyRagDocumentStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatDailyRagDocumentStatsDto(startDate, 4L, 1L, 2L, 1L, 0L, 2.0),
                new AskChatDailyRagDocumentStatsDto(endDate, 2L, 0L, 1L, 0L, 1L, 3.0)
        ));
        when(repository.findDailyRagReferenceStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatDailyRagReferenceStatsDto(startDate, 3L, 2L),
                new AskChatDailyRagReferenceStatsDto(endDate, 1L, 1L)
        ));
        when(repository.findRagSummary(startInclusive, endExclusive))
                .thenReturn(new AskChatRagSummaryDto(6L, 1L, 3L, 1L, 1L, 2.5, 4L, 3L));
        when(repository.findRagSourceStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatRagSourceStatsDto("ASK_CHAT_MESSAGE", 4L, 1L, 2L, 1L, 0L)
        ));
        when(repository.findRagErrorStats(startInclusive, endExclusive)).thenReturn(List.of(
                new AskChatRagErrorStatsDto("EMBEDDING_TIMEOUT", 1L)
        ));

        AskChatStatsViewModel viewModel = service.getAskChatStats(startDate, endDate);

        assertThat(viewModel.startDate()).isEqualTo(startDate);
        assertThat(viewModel.endDate()).isEqualTo(endDate);
        assertThat(viewModel.dailyStats()).hasSize(3);
        assertThat(viewModel.dailyStats().get(0).userMessageCount()).isEqualTo(2L);
        assertThat(viewModel.dailyStats().get(1).date()).isEqualTo(startDate.plusDays(1));
        assertThat(viewModel.dailyStats().get(1).sessionCount()).isZero();
        assertThat(viewModel.dailyStats().get(1).averageGenerationDurationMs()).isZero();
        assertThat(viewModel.dailyStats().get(0).walletStats().paidTurnsConsumed()).isEqualTo(1L);
        assertThat(viewModel.dailyStats().get(1).walletStats().totalLogCount()).isZero();
        assertThat(viewModel.dailyStats().get(0).ragStats().completedDocumentCount()).isEqualTo(2L);
        assertThat(viewModel.dailyStats().get(1).ragStats().referenceCount()).isZero();
        assertThat(viewModel.dailyStats().get(2).p95GenerationDurationMs()).isEqualTo(300L);
        assertThat(viewModel.totalSessionCount()).isEqualTo(3L);
        assertThat(viewModel.totalUniqueUserCount()).isEqualTo(2L);
        assertThat(viewModel.totalActiveSessionCount()).isEqualTo(2L);
        assertThat(viewModel.totalEndedSessionCount()).isEqualTo(1L);
        assertThat(viewModel.totalUserMessageCount()).isEqualTo(3L);
        assertThat(viewModel.totalCompletedAssistantMessageCount()).isEqualTo(3L);
        assertThat(viewModel.totalFailedAssistantMessageCount()).isEqualTo(1L);
        assertThat(viewModel.assistantSuccessRatePercent()).isEqualTo(75.0);
        assertThat(viewModel.averageGenerationDurationMs()).isEqualTo(200.0);
        assertThat(viewModel.p95GenerationDurationMs()).isEqualTo(280L);
        assertThat(viewModel.errorStats()).containsExactly(new AskChatErrorStatsDto("TIMEOUT", 1L));
        assertThat(viewModel.walletStats().paidTurnsConsumed()).isEqualTo(3L);
        assertThat(viewModel.walletStats().paidTurnsCharged()).isEqualTo(9L);
        assertThat(viewModel.walletStats().netPaidTurnDelta()).isEqualTo(-3L);
        assertThat(viewModel.ragStats().totalDocumentCount()).isEqualTo(6L);
        assertThat(viewModel.ragStats().embeddingCompletionRatePercent()).isEqualTo(50.0);
        assertThat(viewModel.ragStats().totalReferenceCount()).isEqualTo(4L);
        assertThat(viewModel.ragStats().sourceStats())
                .containsExactly(new AskChatRagSourceStatsDto("ASK_CHAT_MESSAGE", 4L, 1L, 2L, 1L, 0L));
        assertThat(viewModel.ragStats().errorStats())
                .containsExactly(new AskChatRagErrorStatsDto("EMBEDDING_TIMEOUT", 1L));
        assertThat(viewModel.refreshedAt()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

        verify(repository).findDailySessionStats(startInclusive, endExclusive);
        verify(repository).findDailyMessageStats(startInclusive, endExclusive);
        verify(repository).findSessionSummary(startInclusive, endExclusive);
        verify(repository).findMessageSummary(startInclusive, endExclusive);
        verify(repository).findAssistantErrorStats(startInclusive, endExclusive);
        verify(repository).findDailyWalletStats(startInclusive, endExclusive);
        verify(repository).findWalletSummary(startInclusive, endExclusive);
        verify(repository).findDailyRagDocumentStats(startInclusive, endExclusive);
        verify(repository).findDailyRagReferenceStats(startInclusive, endExclusive);
        verify(repository).findRagSummary(startInclusive, endExclusive);
        verify(repository).findRagSourceStats(startInclusive, endExclusive);
        verify(repository).findRagErrorStats(startInclusive, endExclusive);
    }

    @Test
    void getAskChatStats_rejects_reversed_date_range() {
        AskChatStatsService service = new AskChatStatsService(mock(AskChatStatsRepository.class));

        assertThatThrownBy(() -> service.getAskChatStats(
                LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 8, 10)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must be on or before endDate");
    }
}
