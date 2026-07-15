package com.devkor.ifive.nadab.global.core.prompt.askchat;

public interface AskChatAnswerPromptLoader {
    String loadSystemPrompt();

    String loadUserPrompt();
}
