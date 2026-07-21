package com.devkor.ifive.nadab.global.core.prompt.askchat;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Profile("local")
@Slf4j
public class LocalAskChatAnswerPromptLoader implements AskChatAnswerPromptLoader {

    private static final String SYSTEM_PROMPT_PATH = "secret/ask-chat-answer-system-prompt-local.txt";
    private static final String USER_PROMPT_PATH = "secret/ask-chat-answer-user-prompt-local.txt";

    @Override
    public String loadSystemPrompt() {
        return load(SYSTEM_PROMPT_PATH);
    }

    @Override
    public String loadUserPrompt() {
        return load(USER_PROMPT_PATH);
    }

    private String load(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                log.error("Ask Chat 답변 프롬프트 파일이 존재하지 않습니다: {}", path);
                throw new BadRequestException(ErrorCode.PROMPT_ASK_CHAT_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("로컬 Ask Chat 답변 프롬프트 파일 읽기 실패: {}", path, e);
            throw new BadRequestException(ErrorCode.PROMPT_ASK_CHAT_FILE_READ_FAILED);
        }
    }
}
