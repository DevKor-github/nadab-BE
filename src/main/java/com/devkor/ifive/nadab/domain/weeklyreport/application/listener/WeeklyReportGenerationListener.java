package com.devkor.ifive.nadab.domain.weeklyreport.application.listener;

import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportTxService;
import com.devkor.ifive.nadab.domain.weeklyreport.application.helper.WeeklyEntriesAssembler;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.AiWeeklyReportResultDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.weeklyreport.infra.WeeklyReportLlmClient;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyQueryRepository;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
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
public class WeeklyReportGenerationListener {

    private final WeeklyQueryRepository weeklyQueryRepository;

    private final WeeklyReportLlmClient weeklyReportLlmClient;
    private final WeeklyReportTxService weeklyReportTxService;

    private static final int MAX_LEN = 150;

    @Async("weeklyReportTaskExecutor")
    @TransactionalEventListener(phase =
            TransactionPhase.AFTER_COMMIT)
    public void handle(WeeklyReportGenerationRequestedEventDto event) {

        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();

        List<DailyEntryDto> rows = weeklyQueryRepository.findWeeklyInputs(event.userId(), range.weekStartDate(), range.weekEndDate());
        String entries = WeeklyEntriesAssembler.assemble(rows);

        AiWeeklyReportResultDto dto;
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = weeklyReportLlmClient.generate(range.weekStartDate().toString(), range.weekEndDate().toString(), entries);
        } catch (Exception e) {
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

        // 성공 확정(별도 트랜잭션)
        try {
            weeklyReportTxService.confirmWeekly(
                    event.reportId(),
                    event.crystalLogId(),
                    cut(dto.discovered()),
                    cut(dto.good()),
                    cut(dto.improve())
            );
        } catch (Exception e) {
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

    // 최대 길이 자르기
    private String cut(String s) {
        if (s == null) return null;
        s = s.trim();
        return (s.length() <= MAX_LEN) ? s : s.substring(0, MAX_LEN);
    }
}

