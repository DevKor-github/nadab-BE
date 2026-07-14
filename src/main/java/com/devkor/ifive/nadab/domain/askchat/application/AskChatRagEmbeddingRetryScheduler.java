package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatRagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AskChatRagEmbeddingRetryScheduler {

    private final AskChatRagIndexingService askChatRagIndexingService;
    private final AskChatRagProperties properties;

    @Scheduled(fixedDelayString = "${ask-chat.rag.embedding-retry-fixed-delay-ms:60000}")
    public void retryFailedEmbeddings() {
        if (!properties.isEmbeddingRetryEnabled()) {
            return;
        }

        try {
            int retriedCount = askChatRagIndexingService.retryFailedEmbeddings();
            if (retriedCount > 0) {
                log.info("Ask Chat RAG embedding retry completed: retriedCount={}", retriedCount);
            }
        } catch (Exception e) {
            log.error("[ASK_CHAT_RAG][EMBEDDING_RETRY_FAILED]", e);
        }
    }
}
