package com.devkor.ifive.nadab.global.infra.llm;

public record LlmGenerationResult<T>(
        T content,
        LlmTokenUsage tokenUsage
) {

    public LlmGenerationResult {
        if (tokenUsage == null) {
            tokenUsage = LlmTokenUsage.empty();
        }
    }
}
