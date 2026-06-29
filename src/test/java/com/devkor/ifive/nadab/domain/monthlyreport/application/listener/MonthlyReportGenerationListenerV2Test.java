package com.devkor.ifive.nadab.domain.monthlyreport.application.listener;

import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyImagePresetAssignmentService;
import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportTxServiceV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.AiMonthlyReportResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyImagePromptContext;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDtoV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyQueryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlySocialSummaryService;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlyWeeklySummariesService;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportImageStorage;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportLlmClientV2;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.OpenAiImageClient;
import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportGenerationListenerV2Test {

    @Mock MonthlyQueryRepository monthlyQueryRepository;
    @Mock MonthlyReportV2Repository monthlyReportV2Repository;
    @Mock MonthlyReportLlmClientV2 monthlyReportLlmClientV2;
    @Mock OpenAiImageClient openAiImageClient;
    @Mock MonthlyReportImageStorage monthlyReportImageStorage;
    @Mock MonthlyReportTxServiceV2 monthlyReportTxServiceV2;
    @Mock MonthlyImagePresetAssignmentService monthlyImagePresetAssignmentService;
    @Mock MonthlyWeeklySummariesService monthlyWeeklySummariesService;
    @Mock MonthlySocialSummaryService monthlySocialSummaryService;
    @Mock ReportGenerationLogRecorder reportGenerationLogRecorder;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    MonthlyReportGenerationListenerV2 listener;

    @Test
    void 할당된_프리셋으로_이미지_prompt_context를_생성한다() {
        MonthlyReportGenerationRequestedEventDtoV2 event =
                new MonthlyReportGenerationRequestedEventDtoV2(10L, 1L, 100L, null);
        MonthlyReportV2Content content = new MonthlyReportV2Content(
                "월간 요약",
                "코멘트 요약",
                "성장",
                "",
                new StyledText(List.of()),
                new StyledText(List.of())
        );
        AiMonthlyReportResultDto result = new AiMonthlyReportResultDto(content, null);

        when(monthlyQueryRepository.findMonthlyInputs(anyLong(), any(), any())).thenReturn(List.of());
        when(monthlyQueryRepository.countCompletedEmotionStatsByRange(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        when(monthlyQueryRepository.countCompletedInterestStatsByRange(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        when(monthlyWeeklySummariesService.buildWeeklySummaries(anyLong(), any())).thenReturn("");
        when(monthlySocialSummaryService.buildSocialSummary(anyLong(), any()))
                .thenReturn(MonthlySocialSummaryContent.empty(1));
        when(monthlyReportLlmClientV2.generate(any(), any(), any(), any(), any(), any()))
                .thenReturn(result);
        when(monthlyImagePresetAssignmentService.getOrAssign(1L, 10L))
                .thenReturn(MonthlyImageStylePreset.INK_WASH);
        when(openAiImageClient.generateBase64Image(anyLong(), any(MonthlyImagePromptContext.class)))
                .thenReturn("base64-image");
        when(monthlyReportImageStorage.uploadBase64Webp(1L, 10L, "base64-image"))
                .thenReturn("monthly/1/10.webp");

        listener.handle(event);

        ArgumentCaptor<MonthlyImagePromptContext> contextCaptor =
                ArgumentCaptor.forClass(MonthlyImagePromptContext.class);
        verify(openAiImageClient).generateBase64Image(anyLong(), contextCaptor.capture());
        MonthlyImagePromptContext context = contextCaptor.getValue();
        assertThat(context.summary()).isEqualTo("월간 요약");
        assertThat(context.commentSummary()).isEqualTo("코멘트 요약");
        assertThat(context.dominantKeyword()).isEqualTo("성장");
        assertThat(context.stylePreset()).isEqualTo(MonthlyImageStylePreset.INK_WASH);
        verify(monthlyReportTxServiceV2).confirmMonthly(10L, 100L, "monthly/1/10.webp");
    }

    @Test
    void 프리셋_할당에_실패하면_이미지_생성을_호출하지_않고_환불한다() {
        MonthlyReportGenerationRequestedEventDtoV2 event =
                new MonthlyReportGenerationRequestedEventDtoV2(10L, 1L, 100L, null);
        MonthlyReportV2Content content = new MonthlyReportV2Content(
                "월간 요약",
                "코멘트 요약",
                "성장",
                "",
                new StyledText(List.of()),
                new StyledText(List.of())
        );

        when(monthlyQueryRepository.findMonthlyInputs(anyLong(), any(), any())).thenReturn(List.of());
        when(monthlyQueryRepository.countCompletedEmotionStatsByRange(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        when(monthlyQueryRepository.countCompletedInterestStatsByRange(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        when(monthlyWeeklySummariesService.buildWeeklySummaries(anyLong(), any())).thenReturn("");
        when(monthlySocialSummaryService.buildSocialSummary(anyLong(), any()))
                .thenReturn(MonthlySocialSummaryContent.empty(1));
        when(monthlyReportLlmClientV2.generate(any(), any(), any(), any(), any(), any()))
                .thenReturn(new AiMonthlyReportResultDto(content, null));
        when(monthlyImagePresetAssignmentService.getOrAssign(1L, 10L))
                .thenThrow(new IllegalStateException("preset assignment failed"));

        listener.handle(event);

        verify(monthlyReportTxServiceV2).failAndRefundMonthlyWithImage(1L, 10L, 100L);
        verifyNoInteractions(openAiImageClient);
    }
}
