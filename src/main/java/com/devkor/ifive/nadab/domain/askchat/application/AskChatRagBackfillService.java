package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillResultDto;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillTargetDto;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagBackfillQueryRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatEmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AskChatRagBackfillService {

    private final AskChatRagBackfillQueryRepository backfillQueryRepository;
    private final AskChatRagIndexingService indexingService;
    private final AskChatEmbeddingClient embeddingClient;

    public AskChatRagBackfillResultDto backfillCompletedDailyAnswers() {
        List<AskChatRagBackfillTargetDto> targets = backfillQueryRepository.findCompletedDailyAnswerTargets(
                embeddingClient.version(),
                embeddingClient.backfillBatchSize()
        );

        int indexedCount = 0;
        int failedCount = 0;
        for (AskChatRagBackfillTargetDto target : targets) {
            try {
                indexingService.indexDailyAnswer(
                        target.answerEntryId(),
                        target.reportId(),
                        target.interestCode()
                );
                indexedCount++;
            } catch (Exception e) {
                failedCount++;
                log.error("[ASK_CHAT_RAG][BACKFILL_DAILY_ANSWER_FAILED] answerEntryId={}, reportId={}",
                        target.answerEntryId(), target.reportId(), e);
            }
        }

        return new AskChatRagBackfillResultDto(targets.size(), indexedCount, failedCount);
    }
}
