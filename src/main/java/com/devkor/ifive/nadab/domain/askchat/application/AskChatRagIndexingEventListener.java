package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.dailyreport.application.event.DailyReportCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AskChatRagIndexingEventListener {

    private final AskChatRagIndexingService askChatRagIndexingService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DailyReportCompletedEvent event) {
        try {
            askChatRagIndexingService.indexDailyAnswer(
                    event.getAnswerEntryId(),
                    event.getReportId(),
                    event.getInterestCode()
            );
        } catch (Exception e) {
            log.error("[ASK_CHAT_RAG][DAILY_ANSWER_INDEX_FAILED] answerEntryId={}, reportId={}",
                    event.getAnswerEntryId(), event.getReportId(), e);
        }
    }
}
