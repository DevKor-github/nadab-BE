package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatStatsViewModel;
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

        AskChatStatsViewModel viewModel = service.getAskChatStats(startDate, endDate);

        assertThat(viewModel.startDate()).isEqualTo(startDate);
        assertThat(viewModel.endDate()).isEqualTo(endDate);
        assertThat(viewModel.dailyStats()).hasSize(3);
        assertThat(viewModel.dailyStats().get(0).userMessageCount()).isEqualTo(2L);
        assertThat(viewModel.dailyStats().get(1).date()).isEqualTo(startDate.plusDays(1));
        assertThat(viewModel.dailyStats().get(1).sessionCount()).isZero();
        assertThat(viewModel.dailyStats().get(1).averageGenerationDurationMs()).isZero();
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
        assertThat(viewModel.refreshedAt()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

        verify(repository).findDailySessionStats(startInclusive, endExclusive);
        verify(repository).findDailyMessageStats(startInclusive, endExclusive);
        verify(repository).findSessionSummary(startInclusive, endExclusive);
        verify(repository).findMessageSummary(startInclusive, endExclusive);
        verify(repository).findAssistantErrorStats(startInclusive, endExclusive);
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
