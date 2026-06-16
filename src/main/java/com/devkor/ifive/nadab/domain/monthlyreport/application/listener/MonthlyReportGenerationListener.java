package com.devkor.ifive.nadab.domain.monthlyreport.application.listener;

import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportTxService;
import com.devkor.ifive.nadab.domain.monthlyreport.application.event.MonthlyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyRepresentativePicker;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyQueryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlyWeeklySummariesService;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportLlmClient;
import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.weeklyreport.application.helper.WeeklyEntriesAssembler;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.shared.reportcontent.AiReportResultDto;
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

    private static final String MONTHLY_REPORT_LLM_MODEL = "GEMINI_2_5_FLASH";

    private final MonthlyQueryRepository monthlyQueryRepository;

    private final MonthlyReportLlmClient monthlyReportLlmClient;
    private final MonthlyReportTxService monthlyReportTxService;
    private final MonthlyWeeklySummariesService monthlyWeeklySummariesService;
    private final ReportGenerationLogRecorder reportGenerationLogRecorder;
    private final ApplicationEventPublisher eventPublisher;

    @Async("monthlyReportTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MonthlyReportGenerationRequestedEventDto event) {

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();

        // 1. 일간 리포트 대표 항목 선택
        List<DailyEntryDto> rows = monthlyQueryRepository.findMonthlyInputs(event.userId(), range.monthStartDate(), range.monthEndDate());
        List<DailyEntryDto> entries = MonthlyRepresentativePicker.pick(rows, 6);
        String representativeEntries = WeeklyEntriesAssembler.assemble(entries);

        // 2. 주간 리포트 선택
        String weeklySummaries = monthlyWeeklySummariesService.buildWeeklySummaries(event.userId(), range);

        AiReportResultDto dto;
        Long generationLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.MONTHLY,
                event.reportId(),
                ReportGenerationStep.MONTHLY_GENERATE,
                LlmProvider.GEMINI,
                MONTHLY_REPORT_LLM_MODEL,
                null
        );
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = monthlyReportLlmClient.generate(
                    range.monthStartDate().toString(),
                    range.monthEndDate().toString(),
                    weeklySummaries,
                    representativeEntries
            );
            reportGenerationLogRecorder.succeed(generationLogId);
        } catch (Exception e) {
            reportGenerationLogRecorder.fail(generationLogId, e);
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

        Long confirmLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.MONTHLY,
                event.reportId(),
                ReportGenerationStep.MONTHLY_CONFIRM,
                null,
                null,
                null
        );
        try {
            monthlyReportTxService.confirmMonthly(
                    event.reportId(),
                    event.crystalLogId(),
                    dto.content()
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
            monthlyReportTxService.failAndRefundMonthly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
        }
    }
}
