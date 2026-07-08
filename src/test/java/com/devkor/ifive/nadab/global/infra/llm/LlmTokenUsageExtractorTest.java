package com.devkor.ifive.nadab.global.infra.llm;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlmTokenUsageExtractorTest {

    @Test
    void extract_maps_prompt_completion_total_tokens() {
        // given
        ChatResponse response = new ChatResponse(
                List.of(),
                ChatResponseMetadata.builder()
                        .usage(new DefaultUsage(100, 50, 150))
                        .build()
        );

        // when
        LlmTokenUsage usage = LlmTokenUsageExtractor.extract(response);

        // then
        assertThat(usage.inputTokens()).isEqualTo(100L);
        assertThat(usage.outputTokens()).isEqualTo(50L);
        assertThat(usage.totalTokens()).isEqualTo(150L);
    }

    @Test
    void extract_returns_empty_usage_when_response_is_null() {
        // when
        LlmTokenUsage usage = LlmTokenUsageExtractor.extract((ChatResponse) null);

        // then
        assertThat(usage.inputTokens()).isNull();
        assertThat(usage.outputTokens()).isNull();
        assertThat(usage.totalTokens()).isNull();
    }

    @Test
    void extract_returns_empty_usage_for_empty_usage_metadata() {
        // when
        LlmTokenUsage usage = LlmTokenUsageExtractor.extract(new EmptyUsage());

        // then
        assertThat(usage.inputTokens()).isNull();
        assertThat(usage.outputTokens()).isNull();
        assertThat(usage.totalTokens()).isNull();
    }

    @Test
    void extract_preserves_partially_missing_usage_values() {
        // given
        Usage partialUsage = new Usage() {
            @Override
            public Integer getPromptTokens() {
                return null;
            }

            @Override
            public Integer getCompletionTokens() {
                return 50;
            }

            @Override
            public Integer getTotalTokens() {
                return null;
            }

            @Override
            public Object getNativeUsage() {
                return null;
            }
        };

        // when
        LlmTokenUsage usage = LlmTokenUsageExtractor.extract(partialUsage);

        // then
        assertThat(usage.inputTokens()).isNull();
        assertThat(usage.outputTokens()).isEqualTo(50L);
        assertThat(usage.totalTokens()).isNull();
    }

    @Test
    void token_usage_plus_sums_existing_values_and_preserves_missing_values() {
        // when
        LlmTokenUsage usage = new LlmTokenUsage(100L, null, 150L)
                .plus(new LlmTokenUsage(30L, 20L, null));

        // then
        assertThat(usage.inputTokens()).isEqualTo(130L);
        assertThat(usage.outputTokens()).isEqualTo(20L);
        assertThat(usage.totalTokens()).isEqualTo(150L);
    }

    @Test
    void generation_result_defaults_null_token_usage_to_empty() {
        // when
        LlmGenerationResult<String> result = new LlmGenerationResult<>("content", null);

        // then
        assertThat(result.content()).isEqualTo("content");
        assertThat(result.tokenUsage().inputTokens()).isNull();
        assertThat(result.tokenUsage().outputTokens()).isNull();
        assertThat(result.tokenUsage().totalTokens()).isNull();
    }
}
