package com.devkor.ifive.nadab.domain.askchat.infra;

import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatRagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AskChatEmbeddingClient {

    private final OpenAiEmbeddingModel embeddingModel;
    private final AskChatRagProperties properties;

    public List<Double> embed(String content) {
        return toDoubleList(embeddingModel.embed(content));
    }

    public List<List<Double>> embedAll(List<String> contents) {
        return contents.stream()
                .map(this::embed)
                .toList();
    }

    public String model() {
        return properties.getEmbeddingModel();
    }

    public int version() {
        return properties.getEmbeddingVersion();
    }

    public int dimensions() {
        return properties.getEmbeddingDimensions();
    }

    public int batchSize() {
        return properties.getEmbeddingBatchSize();
    }

    public int backfillBatchSize() {
        return properties.getBackfillBatchSize();
    }

    public int retrievalLimit() {
        return properties.getRetrievalLimit();
    }

    public int maxRetryCount() {
        return properties.getEmbeddingMaxRetryCount();
    }

    private List<Double> toDoubleList(float[] embedding) {
        Double[] values = new Double[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            values[i] = (double) embedding[i];
        }
        return List.of(values);
    }
}
