package com.devkor.ifive.nadab.domain.weeklyreport.application.listener;

import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportTxService;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyQueryRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.infra.WeeklyReportLlmClient;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import com.devkor.ifive.nadab.global.shared.reportcontent.AiReportResultDto;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReportGenerationListenerTest {

    @Mock
    WeeklyQueryRepository weeklyQueryRepository;

    @Mock
    WeeklyReportLlmClient weeklyReportLlmClient;

    @Mock
    WeeklyReportTxService weeklyReportTxService;

    @Mock
    ReportGenerationLogRecorder reportGenerationLogRecorder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    WeeklyReportGenerationListener listener;

    @Test
    void handle_records_token_usage_before_generation_log_succeeds() {
        WeeklyReportGenerationRequestedEventDto event =
                new WeeklyReportGenerationRequestedEventDto(10L, 1L, 100L);
        AiReportResultDto aiResult = aiResult();

        when(weeklyQueryRepository.findWeeklyInputs(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(dailyEntry()));
        when(reportGenerationLogRecorder.start(anyLong(), any(), anyLong(), any(), any(), any()))
                .thenReturn(200L, 201L);
        when(weeklyReportLlmClient.generate(anyString(), anyString(), anyString()))
                .thenReturn(new LlmGenerationResult<>(aiResult, new LlmTokenUsage(100L, 50L, 150L, 30L)));

        listener.handle(event);

        InOrder inOrder = inOrder(reportGenerationLogRecorder);
        inOrder.verify(reportGenerationLogRecorder).recordTokenUsage(200L, 100L, 50L, 150L, 30L);
        inOrder.verify(reportGenerationLogRecorder).succeed(200L);
        verify(weeklyReportTxService).confirmWeekly(10L, 100L, aiResult.content());
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    private DailyEntryDto dailyEntry() {
        return new DailyEntryDto(
                LocalDate.of(2026, 1, 1),
                "question",
                "answer",
                "message",
                null
        );
    }

    private AiReportResultDto aiResult() {
        ReportContent content = new ReportContent(
                "steady growth",
                new StyledText(List.of(new Segment("a".repeat(150), List.of()))),
                new StyledText(List.of(new Segment("b".repeat(80), List.of())))
        );
        return new AiReportResultDto(content, content.discovered().plainText(), content.improve().plainText());
    }
}
