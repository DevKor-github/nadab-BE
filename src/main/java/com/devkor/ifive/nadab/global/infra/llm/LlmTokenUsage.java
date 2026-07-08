package com.devkor.ifive.nadab.global.infra.llm;

public record LlmTokenUsage(
        Long inputTokens,
        Long outputTokens,
        Long totalTokens
) {

    public static LlmTokenUsage empty() {
        return new LlmTokenUsage(null, null, null);
    }

    public LlmTokenUsage plus(LlmTokenUsage other) {
        if (other == null) {
            return this;
        }
        return new LlmTokenUsage(
                add(inputTokens, other.inputTokens),
                add(outputTokens, other.outputTokens),
                add(totalTokens, other.totalTokens)
        );
    }

    private Long add(Long left, Long right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left + right;
    }
}
