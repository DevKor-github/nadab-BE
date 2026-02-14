package com.devkor.ifive.nadab.global.infra.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmRouter {

    @Qualifier("openaiChatClient")
    private final ChatClient openai;

    @Qualifier("claudeChatClient")
    private final ChatClient claude;

    @Qualifier("geminiChatClient")
    private final ChatClient gemini;

    public ChatClient route(LlmProvider provider) {
        return switch (provider) {
            case OPENAI -> openai;
            case CLAUDE -> claude;
            case GEMINI -> gemini;
        };
    }
}