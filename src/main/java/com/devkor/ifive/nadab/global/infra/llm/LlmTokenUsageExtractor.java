package com.devkor.ifive.nadab.global.infra.llm;

import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.metadata.GoogleGenAiUsage;

public final class LlmTokenUsageExtractor {

    private LlmTokenUsageExtractor() {
    }

    public static LlmTokenUsage extract(ChatResponse response) {
        if (response == null || response.getMetadata() == null) {
            return LlmTokenUsage.empty();
        }
        return extract(response.getMetadata().getUsage());
    }

    public static LlmTokenUsage extract(Usage usage) {
        if (usage == null || usage instanceof EmptyUsage) {
            return LlmTokenUsage.empty();
        }

        return new LlmTokenUsage(
                toLong(usage.getPromptTokens()),
                toLong(usage.getCompletionTokens()),
                toLong(usage.getTotalTokens()),
                extractThinkingTokens(usage)
        );
    }

    private static Long extractThinkingTokens(Usage usage) {
        if (usage instanceof GoogleGenAiUsage googleUsage) {
            return toLong(googleUsage.getThoughtsTokenCount());
        }
        return null;
    }

    private static Long toLong(Integer value) {
        return value == null ? null : value.longValue();
    }
}
