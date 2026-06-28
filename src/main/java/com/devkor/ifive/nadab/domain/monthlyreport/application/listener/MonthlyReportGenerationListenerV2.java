package com.devkor.ifive.nadab.domain.monthlyreport.application.listener;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportTxServiceV2;
import com.devkor.ifive.nadab.domain.monthlyreport.application.event.MonthlyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyInterestStatsCalculator;
import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyReportComparisonInputAssembler;
import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyRepresentativePicker;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.InterestStatsContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.AiMonthlyReportResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDtoV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyQueryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlyWeeklySummariesService;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlySocialSummaryService;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportImageStorage;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportLlmClientV2;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.OpenAiImageClient;
import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.typereport.application.helper.TypeEmotionStatsCalculator;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.weeklyreport.application.helper.WeeklyEntriesAssembler;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyReportGenerationListenerV2 {

    private static final String MONTHLY_REPORT_V2_LLM_MODEL = "GEMINI_2_5_FLASH";

    private final MonthlyQueryRepository monthlyQueryRepository;
    private final MonthlyReportV2Repository monthlyReportV2Repository;

    private final MonthlyReportLlmClientV2 monthlyReportLlmClientV2;
    private final OpenAiImageClient openAiImageClient;
    private final MonthlyReportImageStorage monthlyReportImageStorage;

    private final MonthlyReportTxServiceV2 monthlyReportTxServiceV2;
    private final MonthlyWeeklySummariesService monthlyWeeklySummariesService;
    private final MonthlySocialSummaryService monthlySocialSummaryService;
    private final ReportGenerationLogRecorder reportGenerationLogRecorder;
    private final ApplicationEventPublisher eventPublisher;

    @Async("monthlyReportTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MonthlyReportGenerationRequestedEventDtoV2 event) {

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();

        // 1. 일간 리포트 대표 항목 선택
        List<DailyEntryDto> rows = monthlyQueryRepository.findMonthlyInputs(event.userId(), range.monthStartDate(), range.monthEndDate());
        List<DailyEntryDto> entries = MonthlyRepresentativePicker.pick(rows, 6);
        String representativeEntries = WeeklyEntriesAssembler.assemble(entries);

        // 2. 주간 리포트 선택
        String weeklySummaries = monthlyWeeklySummariesService.buildWeeklySummaries(event.userId(), range);

        // 3. 해당 월(COMPLETED DailyReport) 감정 통계 집계
        TypeEmotionStatsContent emotionStats;
        try {
            emotionStats = TypeEmotionStatsCalculator.calculate(
                    monthlyQueryRepository.countCompletedEmotionStatsByRange(
                            event.userId(),
                            DailyReportStatus.COMPLETED,
                            range.monthStartDate(),
                            range.monthEndDate()
                    )
            );
        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][EMOTION_STATS_FAILED] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);
            monthlyReportTxServiceV2.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        InterestStatsContent interestStats;
        try {
            interestStats = MonthlyInterestStatsCalculator.calculate(
                    monthlyQueryRepository.countCompletedInterestStatsByRange(
                            event.userId(),
                            DailyReportStatus.COMPLETED,
                            range.monthStartDate(),
                            range.monthEndDate()
                    )
            );
        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][INTEREST_STATS_FAILED] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);
            monthlyReportTxServiceV2.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        MonthlySocialSummaryContent socialSummary;
        try {
            socialSummary = monthlySocialSummaryService.buildSocialSummary(event.userId(), range);
        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][SOCIAL_SUMMARY_FAILED] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);
            monthlyReportTxServiceV2.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        MonthlyReportComparisonInputDto comparisonInput = null;
        if (event.previousReportId() != null) {
            try {
                comparisonInput = monthlyReportV2Repository.findById(event.previousReportId())
                        .filter(previousReport -> previousReport.getUser() != null
                                && event.userId().equals(previousReport.getUser().getId()))
                        .map(previousReport -> MonthlyReportComparisonInputAssembler.assemble(
                                previousReport,
                                emotionStats
                        ))
                        .orElseThrow();
            } catch (Exception e) {
                log.error("[MONTHLY_REPORT][COMPARISON_INPUT_FAILED] userId={}, reportId={}, previousReportId={}",
                        event.userId(), event.reportId(), event.previousReportId(), e);
                monthlyReportTxServiceV2.failAndRefundMonthly(
                        event.userId(),
                        event.reportId(),
                        event.crystalLogId()
                );
                return;
            }
        }

        AiMonthlyReportResultDto dto;
        Long generationLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.MONTHLY_V2,
                event.reportId(),
                ReportGenerationStep.MONTHLY_V2_GENERATE,
                LlmProvider.GEMINI,
                MONTHLY_REPORT_V2_LLM_MODEL
        );
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = monthlyReportLlmClientV2.generate(
                    range.monthStartDate().toString(),
                    range.monthEndDate().toString(),
                    weeklySummaries,
                    representativeEntries,
                    emotionStats,
                    comparisonInput
            );
            reportGenerationLogRecorder.succeed(generationLogId);
        } catch (Exception e) {
            reportGenerationLogRecorder.fail(generationLogId, e);
            log.error("[MONTHLY_REPORT][LLM_FAILED] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);

            // 실패 확정 + 환불은 별도 트랜잭션에서
            monthlyReportTxServiceV2.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        Long textConfirmLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.MONTHLY_V2,
                event.reportId(),
                ReportGenerationStep.MONTHLY_V2_TEXT_CONFIRM,
                null,
                null
        );
        try {
            monthlyReportTxServiceV2.confirmMonthlyText(
                    event.reportId(),
                    dto.content(),
                    dto.emotionSummaryContent(),
                    emotionStats,
                    interestStats,
                    MonthlyEmotionComparisonContent.from(comparisonInput),
                    socialSummary
            );
            reportGenerationLogRecorder.succeed(textConfirmLogId);

        } catch (Exception e) {
            reportGenerationLogRecorder.fail(textConfirmLogId, e);
            log.error("[MONTHLY_REPORT][TEXT_CONFIRM_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            // 저장 실패면 결과를 못 주는 거니까 "실패 확정 + 환불"로 처리
            monthlyReportTxServiceV2.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        String imageKey;
        Long imageLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.MONTHLY_V2,
                event.reportId(),
                ReportGenerationStep.MONTHLY_V2_IMAGE_GENERATE,
                LlmProvider.OPENAI,
                null
        );
        try {
            String base64Image = openAiImageClient.generateBase64Image(event.userId(), dto, range);
            imageKey = monthlyReportImageStorage.uploadBase64Webp(
                    event.userId(),
                    event.reportId(),
                    base64Image
            );
            reportGenerationLogRecorder.succeed(imageLogId);

        } catch (Exception e) {
            reportGenerationLogRecorder.fail(imageLogId, e);
            log.error("[MONTHLY_REPORT][IMAGE_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            monthlyReportTxServiceV2.failAndRefundMonthlyWithImage(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        Long confirmLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.MONTHLY_V2,
                event.reportId(),
                ReportGenerationStep.MONTHLY_V2_CONFIRM,
                null,
                null
        );
        try {
            monthlyReportTxServiceV2.confirmMonthly(
                    event.reportId(),
                    event.crystalLogId(),
                    imageKey
            );

            // 월간 리포트 완성 이벤트 발행
            eventPublisher.publishEvent(
                    new MonthlyReportCompletedEvent(event.reportId(), event.userId())
            );
            reportGenerationLogRecorder.succeed(confirmLogId);

        } catch (Exception e) {
            reportGenerationLogRecorder.fail(confirmLogId, e);
            log.error("[MONTHLY_REPORT][CONFIRM_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            // 저장 실패면 결과를 못 주는 거니까 "실패 확정 + 환불"로 처리
            monthlyReportTxServiceV2.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
        }
    }
}
