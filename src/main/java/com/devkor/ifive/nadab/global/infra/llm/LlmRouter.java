package com.devkor.ifive.nadab.global.infra.llm;

import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Getter
public class LlmRouter {

    private final ChatClient openai;
    private final ChatClient claude;
    private final ChatClient gemini;

    public LlmRouter(
            @Qualifier("openaiChatClient") ChatClient openai,
            @Qualifier("claudeChatClient") ChatClient claude,
            @Qualifier("geminiChatClient") ChatClient gemini
    ) {
        this.openai = openai;
        this.claude = claude;
        this.gemini = gemini;
    }

    public ChatClient route(LlmProvider provider) {
        return switch (provider) {
            case OPENAI -> openai;
            case CLAUDE -> claude;
            case GEMINI -> gemini;
        };
    }
}