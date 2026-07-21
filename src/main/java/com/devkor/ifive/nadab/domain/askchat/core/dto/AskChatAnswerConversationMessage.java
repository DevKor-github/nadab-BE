package com.devkor.ifive.nadab.domain.askchat.core.dto;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;

public record AskChatAnswerConversationMessage(
        AskChatMessageRole role,
        String content
) {
}
