package com.devkor.ifive.nadab.domain.typereport.application.listener;

import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.domain.typereport.application.TypeReportTxService;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportContentDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeSelectionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.repository.AnalysisTypeRepository;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeDailyEntryQueryRepository;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.typereport.core.service.EvidenceCardGenerationService;
import com.devkor.ifive.nadab.domain.typereport.core.service.PatternExtractionService;
import com.devkor.ifive.nadab.domain.typereport.core.service.TypeReportContentGenerationService;
import com.devkor.ifive.nadab.domain.typereport.core.service.TypeSelectionService;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeReportGenerationListenerTest {

    @Mock
    TypeReportTxService typeReportTxService;

    @Mock
    TypeReportRepository typeReportRepository;

    @Mock
    TypeDailyEntryQueryRepository typeDailyEntryQueryRepository;

    @Mock
    AnalysisTypeRepository analysisTypeRepository;

    @Mock
    EvidenceCardGenerationService evidenceCardGenerationService;

    @Mock
    PatternExtractionService patternExtractionService;

    @Mock
    TypeSelectionService typeSelectionService;

    @Mock
    TypeReportContentGenerationService typeReportContentGenerationService;

    @Mock
    ReportGenerationLogRecorder reportGenerationLogRecorder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    TypeReportGenerationListener listener;

    @Test
    void handle_records_token_usage_for_each_llm_step_before_succeeding_logs() {
        // given
        TypeReportGenerationRequestedEventDto event =
                new TypeReportGenerationRequestedEventDto(10L, 1L, 100L, null);
        TypeReport report = mock(TypeReport.class);
        DailyEntryDto entry = dailyEntry();
        EvidenceCardDto card = new EvidenceCardDto("D1", LocalDate.of(2026, 1, 1), "card");
        PatternExtractionResultDto patterns = new PatternExtractionResultDto(
                List.of(new PatternExtractionResultDto.PatternDto("pattern", List.of("D1"), "note"))
        );
        AnalysisTypeCandidateDto selectedType = new AnalysisTypeCandidateDto(
                "TYPE_A", "name", "description", "#a", "#b", "#c"
        );
        TypeSelectionResultDto selection = new TypeSelectionResultDto(
                "TYPE_A",
                90,
                List.of(new TypeSelectionResultDto.BecauseDto("pattern", List.of("D1")))
        );
        TypeReportContentDto content = typeReportContent();

        when(typeReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(report.getInterestCode()).thenReturn(InterestCode.ROUTINE);
        when(typeDailyEntryQueryRepository.findRecentDailyEntriesByInterest(eq(1L), eq(InterestCode.ROUTINE), any()))
                .thenReturn(List.of(entry));
        when(typeDailyEntryQueryRepository.countCompletedEmotionStatsByInterest(anyLong(), eq(InterestCode.ROUTINE), any()))
                .thenReturn(List.of());
        when(reportGenerationLogRecorder.start(anyLong(), any(), anyLong(), any(), any(), any()))
                .thenReturn(200L, 201L, 202L, 203L, 204L);
        when(evidenceCardGenerationService.generate(List.of(entry)))
                .thenReturn(new LlmGenerationResult<>(List.of(card), new LlmTokenUsage(10L, 5L, 15L, null)));
        when(patternExtractionService.extract(List.of(card)))
                .thenReturn(new LlmGenerationResult<>(patterns, new LlmTokenUsage(20L, 6L, 26L, null)));
        when(analysisTypeRepository.findCandidatesByInterestCode(InterestCode.ROUTINE))
                .thenReturn(List.of(selectedType));
        when(typeSelectionService.select(List.of(selectedType), patterns))
                .thenReturn(new LlmGenerationResult<>(selection, new LlmTokenUsage(30L, 7L, 37L, null)));
        when(typeReportContentGenerationService.generate(selectedType, patterns, List.of(card), TypeContentFactory.emptyEmotionStats().normalized(), "TYPE_A"))
                .thenReturn(new LlmGenerationResult<>(content, new LlmTokenUsage(40L, 8L, 80L, 32L)));

        // when
        listener.handle(event);

        // then
        InOrder inOrder = inOrder(reportGenerationLogRecorder);
        inOrder.verify(reportGenerationLogRecorder).recordTokenUsage(200L, 10L, 5L, 15L, null);
        inOrder.verify(reportGenerationLogRecorder).succeed(200L);
        inOrder.verify(reportGenerationLogRecorder).recordTokenUsage(201L, 20L, 6L, 26L, null);
        inOrder.verify(reportGenerationLogRecorder).succeed(201L);
        inOrder.verify(reportGenerationLogRecorder).recordTokenUsage(202L, 30L, 7L, 37L, null);
        inOrder.verify(reportGenerationLogRecorder).succeed(202L);
        inOrder.verify(reportGenerationLogRecorder).recordTokenUsage(203L, 40L, 8L, 80L, 32L);
        inOrder.verify(reportGenerationLogRecorder).succeed(203L);
        inOrder.verify(reportGenerationLogRecorder).succeed(204L);

        verify(typeReportTxService).confirmType(
                10L,
                100L,
                null,
                "TYPE_A",
                content.typeAnalysis(),
                content.typeAnalysisContent(),
                content.emotionSummaryContent(),
                TypeContentFactory.emptyEmotionStats().normalized(),
                "persona1",
                "content1",
                "persona2",
                "content2"
        );
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    private DailyEntryDto dailyEntry() {
        return new DailyEntryDto(
                LocalDate.of(2026, 1, 1),
                "question",
                "answer",
                "daily report",
                null
        );
    }

    private TypeReportContentDto typeReportContent() {
        return new TypeReportContentDto(
                "TYPE_A",
                "type analysis",
                TypeContentFactory.fromPlainText("type analysis").normalized(),
                TypeContentFactory.emptyText().normalized(),
                List.of(
                        new TypeReportContentDto.PersonaDto("persona1", "content1"),
                        new TypeReportContentDto.PersonaDto("persona2", "content2")
                )
        );
    }
}
