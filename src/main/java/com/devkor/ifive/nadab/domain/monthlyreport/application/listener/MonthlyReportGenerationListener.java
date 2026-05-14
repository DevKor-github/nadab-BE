package com.devkor.ifive.nadab.domain.monthlyreport.application.listener;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportTxService;
import com.devkor.ifive.nadab.domain.monthlyreport.application.event.MonthlyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyRepresentativePicker;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.AiMonthlyReportResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyQueryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlyWeeklySummariesService;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportImageStorage;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportLlmClient;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.OpenAiImageClient;
import com.devkor.ifive.nadab.domain.typereport.application.helper.TypeEmotionStatsCalculator;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.weeklyreport.application.helper.WeeklyEntriesAssembler;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
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
public class MonthlyReportGenerationListener {

    private final MonthlyQueryRepository monthlyQueryRepository;

    private final MonthlyReportLlmClient monthlyReportLlmClient;
    private final OpenAiImageClient openAiImageClient;
    private final MonthlyReportImageStorage monthlyReportImageStorage;

    private final MonthlyReportTxService monthlyReportTxService;
    private final MonthlyWeeklySummariesService monthlyWeeklySummariesService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int MAX_LEN = 400;

    @Async("monthlyReportTaskExecutor")
    @TransactionalEventListener(phase =
            TransactionPhase.AFTER_COMMIT)
    public void handle(MonthlyReportGenerationRequestedEventDto event) {

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
            monthlyReportTxService.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        AiMonthlyReportResultDto dto;
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = monthlyReportLlmClient.generate(
                    range.monthStartDate().toString(),
                    range.monthEndDate().toString(),
                    weeklySummaries,
                    representativeEntries,
                    emotionStats,
                    event.exists());
        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][LLM_FAILED] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);

            // 실패 확정 + 환불은 별도 트랜잭션에서
            monthlyReportTxService.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        // 텍스트 생성 성공 확정(별도 트랜잭션)
        try {
            monthlyReportTxService.confirmMonthlyText(
                    event.reportId(),
                    dto.content(),
                    dto.emotionSummaryContent(),
                    emotionStats
            );

        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][TEXT_CONFIRM_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            // 저장 실패면 결과를 못 주는 거니까 "실패 확정 + 환불"로 처리
            monthlyReportTxService.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        String imageKey = "";
        try {
            String base64Image = openAiImageClient.generateBase64Image(event.userId(), dto, range);
            imageKey = monthlyReportImageStorage.uploadBase64Webp(
                    event.userId(),
                    event.reportId(),
                    base64Image
            );

        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][IMAGE_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            monthlyReportTxService.failAndRefundMonthlyWithImage(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        try {
            monthlyReportTxService.confirmMonthly(
                    event.reportId(),
                    event.crystalLogId(),
                    imageKey
            );

            // 월간 리포트 완성 이벤트 발행
            eventPublisher.publishEvent(
                    new MonthlyReportCompletedEvent(event.reportId(), event.userId())
            );

        } catch (Exception e) {
            log.error("[MONTHLY_REPORT][CONFIRM_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            // 저장 실패면 결과를 못 주는 거니까 "실패 확정 + 환불"로 처리
            monthlyReportTxService.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
        }
    }
}

