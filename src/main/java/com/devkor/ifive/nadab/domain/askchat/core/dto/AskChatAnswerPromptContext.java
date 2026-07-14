package com.devkor.ifive.nadab.domain.askchat.core.dto;

import java.util.List;

public record AskChatAnswerPromptContext(
        Long userId,
        Long sessionId,
        String question,
        List<AskChatAnswerConversationMessage> recentMessages,
        List<AskChatAnswerReferenceDocument> referenceDocuments
) {

    public AskChatAnswerPromptContext {
        recentMessages = recentMessages == null ? List.of() : List.copyOf(recentMessages);
        referenceDocuments = referenceDocuments == null ? List.of() : List.copyOf(referenceDocuments);
    }
}
