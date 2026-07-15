package com.devkor.ifive.nadab.global.core.prompt.askchat;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
@Slf4j
@RequiredArgsConstructor
public class SecretAskChatAnswerPromptLoader implements AskChatAnswerPromptLoader {

    @Value("${ASK_CHAT_ANSWER_SYSTEM_PROMPT}")
    private String systemPrompt;

    @Value("${ASK_CHAT_ANSWER_USER_PROMPT}")
    private String userPrompt;

    @Override
    public String loadSystemPrompt() {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            log.error("환경 변수 ASK_CHAT_ANSWER_SYSTEM_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_ASK_CHAT_ENV_VAR_NOT_SET);
        }

        return systemPrompt;
    }

    @Override
    public String loadUserPrompt() {
        if (userPrompt == null || userPrompt.isBlank()) {
            log.error("환경 변수 ASK_CHAT_ANSWER_USER_PROMPT가 비어있습니다.");
            throw new BadRequestException(ErrorCode.PROMPT_ASK_CHAT_ENV_VAR_NOT_SET);
        }

        return userPrompt;
    }
}
