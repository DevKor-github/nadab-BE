package com.devkor.ifive.nadab.domain.askchat.core.dto;

import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;

import java.util.List;

public record AskChatAnswerGenerationResult(
        AskChatGeneratedAnswer answer,
        LlmProvider provider,
        String model,
        LlmTokenUsage tokenUsage,
        List<Long> referenceDocumentIds
) {

    public AskChatAnswerGenerationResult {
        tokenUsage = tokenUsage == null ? LlmTokenUsage.empty() : tokenUsage;
        referenceDocumentIds = referenceDocumentIds == null ? List.of() : List.copyOf(referenceDocumentIds);
    }
}
