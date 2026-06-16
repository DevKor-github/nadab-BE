package com.devkor.ifive.nadab.domain.weeklyreport.application.listener;

import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportTxService;
import com.devkor.ifive.nadab.domain.weeklyreport.application.event.WeeklyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.weeklyreport.application.helper.WeeklyEntriesAssembler;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyQueryRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.infra.WeeklyReportLlmClient;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.shared.reportcontent.AiReportResultDto;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
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
public class WeeklyReportGenerationListener {

    private static final String WEEKLY_REPORT_LLM_MODEL = "GEMINI_2_5_FLASH";

    private final WeeklyQueryRepository weeklyQueryRepository;

    private final WeeklyReportLlmClient weeklyReportLlmClient;
    private final WeeklyReportTxService weeklyReportTxService;
    private final ReportGenerationLogRecorder reportGenerationLogRecorder;
    private final ApplicationEventPublisher eventPublisher;

    @Async("weeklyReportTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WeeklyReportGenerationRequestedEventDto event) {

        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();

        List<DailyEntryDto> rows = weeklyQueryRepository.findWeeklyInputs(event.userId(), range.weekStartDate(), range.weekEndDate());
        String entries = WeeklyEntriesAssembler.assemble(rows);

        AiReportResultDto dto;
        Long generationLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.WEEKLY,
                event.reportId(),
                ReportGenerationStep.WEEKLY_GENERATE,
                LlmProvider.GEMINI,
                WEEKLY_REPORT_LLM_MODEL,
                null
        );
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = weeklyReportLlmClient.generate(range.weekStartDate().toString(), range.weekEndDate().toString(), entries);
            reportGenerationLogRecorder.succeed(generationLogId);
        } catch (Exception e) {
            reportGenerationLogRecorder.fail(generationLogId, e);
            log.error("[WEEKLY_REPORT][LLM_FAILED] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);

            // 실패 확정 + 환불은 별도 트랜잭션에서
            weeklyReportTxService.failAndRefundWeekly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        Long confirmLogId = reportGenerationLogRecorder.start(
                event.userId(),
                ReportGenerationType.WEEKLY,
                event.reportId(),
                ReportGenerationStep.WEEKLY_CONFIRM,
                null,
                null,
                null
        );
        try {
            weeklyReportTxService.confirmWeekly(
                    event.reportId(),
                    event.crystalLogId(),
                    dto.content()
            );

            // 주간 리포트 완성 이벤트 발행
            eventPublisher.publishEvent(
                    new WeeklyReportCompletedEvent(event.reportId(), event.userId())
            );
            reportGenerationLogRecorder.succeed(confirmLogId);

        } catch (Exception e) {
            reportGenerationLogRecorder.fail(confirmLogId, e);
            log.error("[WEEKLY_REPORT][CONFIRM_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);

            // 저장 실패면 결과를 못 주는 거니까 "실패 확정 + 환불"로 처리
            weeklyReportTxService.failAndRefundWeekly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
        }
    }
}
