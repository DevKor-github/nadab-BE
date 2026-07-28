package com.devkor.ifive.nadab.domain.askchat.core.dto;

import java.util.List;

public record AskChatGeneratedAnswer(
        String answer,
        List<String> followUpQuestions
) {

    public AskChatGeneratedAnswer {
        followUpQuestions = followUpQuestions == null ? List.of() : List.copyOf(followUpQuestions);
    }
}
