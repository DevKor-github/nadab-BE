package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatRagProperties;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AskChatRagEmbeddingRetrySchedulerTest {

    @Test
    void retryFailedEmbeddings_calls_indexing_service_when_enabled() {
        AskChatRagIndexingService indexingService = mock(AskChatRagIndexingService.class);
        AskChatRagProperties properties = new AskChatRagProperties();
        properties.setEmbeddingRetryEnabled(true);
        AskChatRagEmbeddingRetryScheduler scheduler = new AskChatRagEmbeddingRetryScheduler(
                indexingService,
                properties
        );
        when(indexingService.retryFailedEmbeddings()).thenReturn(2);

        scheduler.retryFailedEmbeddings();

        verify(indexingService).retryFailedEmbeddings();
    }

    @Test
    void retryFailedEmbeddings_skips_indexing_service_when_disabled() {
        AskChatRagIndexingService indexingService = mock(AskChatRagIndexingService.class);
        AskChatRagProperties properties = new AskChatRagProperties();
        properties.setEmbeddingRetryEnabled(false);
        AskChatRagEmbeddingRetryScheduler scheduler = new AskChatRagEmbeddingRetryScheduler(
                indexingService,
                properties
        );

        scheduler.retryFailedEmbeddings();

        verify(indexingService, never()).retryFailedEmbeddings();
    }
}
