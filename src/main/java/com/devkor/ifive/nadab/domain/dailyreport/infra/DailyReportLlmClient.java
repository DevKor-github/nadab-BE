package com.devkor.ifive.nadab.domain.dailyreport.infra;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.LlmDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class DailyReportLlmClient {

    private final DailyReportPromptLoader dailyReportPromptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    private final LlmProvider provider = LlmProvider.OPENAI;

    public AiDailyReportResultDto generate(String question, AnswerEntry answerEntry) {

        String answer = answerEntry.getContent();

        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        String withImagePrompt = dailyReportPromptLoader.loadWithImagePrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        ChatClient chatClient = llmRouter.route(provider);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                .temperature(0.3)
                .maxTokens(512)
                .build();

        UserMessage userMessage = buildUserMessage(prompt, withImagePrompt,answerEntry);

        String content = chatClient.prompt()
                .messages(userMessage)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            // 3. JSON → DTO 역직렬화
            LlmDailyResultDto result = objectMapper.readValue(content, LlmDailyResultDto.class);

            String message = result.message();

            String emotion = result.emotion();

            if (isBlank(message) || isBlank(emotion)) {
                throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }

            return new AiDailyReportResultDto(
                    message,
                    emotion
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private UserMessage buildUserMessage(String prompt, String withImagePrompt, AnswerEntry answerEntry) {
        String imageKey = answerEntry.getImageKey();

        //이미지 없는 경우
        if (isBlank(imageKey)) {
            return new UserMessage(prompt);
        }

        //이미지 있는 경우
        String imageUrl = profileImageUrlBuilder.buildUrl(imageKey);

        MimeType mimeType = inferMimeType(imageUrl);

        return UserMessage.builder()
                .text(withImagePrompt)
                .media(new Media(mimeType, URI.create(imageUrl)))
                .build();
    }

    private MimeType inferMimeType(String imageUrl) {
        String lower = imageUrl.toLowerCase();

        if (lower.contains(".png")) {
            return MimeTypeUtils.IMAGE_PNG;
        }
        if (lower.contains(".jpg") || lower.contains(".jpeg")) {
            return MimeTypeUtils.IMAGE_JPEG;
        }

        throw new BadRequestException(ErrorCode.IMAGE_UNSUPPORTED_TYPE);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
