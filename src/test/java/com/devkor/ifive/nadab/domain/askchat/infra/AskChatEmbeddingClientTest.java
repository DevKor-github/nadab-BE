package com.devkor.ifive.nadab.domain.askchat.infra;

import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatRagProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiEmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatEmbeddingClientTest {

    @Mock
    OpenAiEmbeddingModel embeddingModel;

    @Test
    void embed_converts_float_embedding_to_double_list() {
        AskChatEmbeddingClient client = new AskChatEmbeddingClient(embeddingModel, properties());
        when(embeddingModel.embed("Who am I?")).thenReturn(new float[]{0.1f, -0.2f, 0.3f});

        List<Double> result = client.embed("Who am I?");

        assertThat(result).containsExactly(
                (double) 0.1f,
                (double) -0.2f,
                (double) 0.3f
        );
    }

    @Test
    void exposes_rag_embedding_options() {
        AskChatRagProperties properties = properties();
        AskChatEmbeddingClient client = new AskChatEmbeddingClient(embeddingModel, properties);

        assertThat(client.model()).isEqualTo("text-embedding-3-small");
        assertThat(client.version()).isEqualTo(1);
        assertThat(client.dimensions()).isEqualTo(1536);
        assertThat(client.batchSize()).isEqualTo(20);
        assertThat(client.retrievalLimit()).isEqualTo(5);
        assertThat(client.maxRetryCount()).isEqualTo(3);
    }

    private AskChatRagProperties properties() {
        AskChatRagProperties properties = new AskChatRagProperties();
        properties.setEmbeddingModel("text-embedding-3-small");
        properties.setEmbeddingDimensions(1536);
        properties.setEmbeddingVersion(1);
        properties.setEmbeddingBatchSize(20);
        properties.setRetrievalLimit(5);
        properties.setEmbeddingMaxRetryCount(3);
        return properties;
    }
}
