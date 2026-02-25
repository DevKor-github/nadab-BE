package com.devkor.ifive.nadab.domain.monthlyreport.application.listener;

import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportTxService;
import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyRepresentativePicker;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyQueryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.MonthlyWeeklySummariesService;
import com.devkor.ifive.nadab.domain.monthlyreport.infra.MonthlyReportLlmClient;
import com.devkor.ifive.nadab.domain.weeklyreport.application.helper.WeeklyEntriesAssembler;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.global.shared.reportcontent.AiReportResultDto;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MonthlyReportTxService monthlyReportTxService;
    private final MonthlyWeeklySummariesService monthlyWeeklySummariesService;

    private static final int MAX_LEN = 245;

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

        AiReportResultDto dto;
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = monthlyReportLlmClient.generate(
                    range.monthStartDate().toString(), range.monthEndDate().toString(), weeklySummaries, representativeEntries);
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

        // 성공 확정(별도 트랜잭션)
        try {
            monthlyReportTxService.confirmMonthly(
                    event.reportId(),
                    event.crystalLogId(),
                    dto.content()
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

    // 최대 길이 자르기
    private String cut(String s) {
        if (s == null) return null;
        s = s.trim();
        return (s.length() <= MAX_LEN) ? s : s.substring(0, MAX_LEN);
    }
}

