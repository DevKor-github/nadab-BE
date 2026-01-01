package com.devkor.ifive.nadab.domain.weeklyreport.application.helper;

import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportTxService;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.AiWeeklyReportResultDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportEntryInputDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.infra.WeeklyReportLlmClient;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyQueryRepository;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyReportGenerationListener {

    private final WeeklyQueryRepository weeklyQueryRepository;

    private final WeeklyReportLlmClient weeklyReportLlmClient;
    private final WeeklyReportTxService weeklyReportTxService;

    @Async
    @TransactionalEventListener(phase =
            TransactionPhase.AFTER_COMMIT)
    public void handle(WeeklyReportGenerationRequestedEventDto event) {

        WeekRangeDto range = WeekRangeCalculator.getCurrentWeekRange();

        List<WeeklyReportEntryInputDto> rows = weeklyQueryRepository.findWeeklyInputs(event.userId(), range.weekStartDate(), range.weekEndDate());
        String entries = WeeklyEntriesAssembler.assemble(rows);

        AiWeeklyReportResultDto dto;
        try {
            // 트랜잭션 밖(백그라운드)에서 LLM 호출
            dto = weeklyReportLlmClient.generate(range.weekStartDate().toString(), range.weekEndDate().toString(), entries);
        } catch (Exception e) {
            // 실패 확정 + 환불은 별도 트랜잭션에서
            weeklyReportTxService.failAndRefundWeekly(
                    event.userId(),
                    event.reportId(),
                    event.crystalLogId()
            );
            return;
        }

        // 성공 확정(별도 트랜잭션)
        weeklyReportTxService.confirmWeekly(
                event.reportId(),
                event.crystalLogId(),
                dto.discovered(),
                dto.good(),
                dto.improve()
        );
    }
}

