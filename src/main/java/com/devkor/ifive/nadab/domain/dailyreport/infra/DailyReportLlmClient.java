package com.devkor.ifive.nadab.domain.dailyreport.infra;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.LlmDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmExceptionMapper;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsageExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
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
    private final DailyReportLlmProperties properties;

    private final LlmProvider provider = LlmProvider.OPENAI;

    public LlmGenerationResult<AiDailyReportResultDto> generate(String question, AnswerEntry answerEntry) {

        String answer = answerEntry.getContent();

        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        String withImagePrompt = dailyReportPromptLoader.loadWithImagePrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        ChatClient chatClient = llmRouter.route(provider);

        OpenAiChatOptions options = buildOptions();

        UserMessage userMessage = buildUserMessage(prompt, withImagePrompt,answerEntry);

        String content;
        LlmTokenUsage tokenUsage;
        try {
            ChatResponse response = chatClient.prompt()
                    .messages(userMessage)
                    .options(options)
                    .call()
                    .chatResponse();
            content = extractContent(response);
            tokenUsage = LlmTokenUsageExtractor.extract(response);
        } catch (Exception e) {
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        }

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

            return new LlmGenerationResult<>(
                    new AiDailyReportResultDto(
                            message,
                            emotion
                    ),
                    tokenUsage
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    public String model() {
        return properties.getModel();
    }

    private OpenAiChatOptions buildOptions() {
        var builder = OpenAiChatOptions.builder()
                .model(properties.getModel())
                .temperature(properties.getTemperature());

        switch (properties.getTokenLimitParameter()) {
            case MAX_TOKENS -> builder.maxTokens(properties.getMaxOutputTokens());
            case MAX_COMPLETION_TOKENS -> builder.maxCompletionTokens(properties.getMaxOutputTokens());
        }

        if (!isBlank(properties.getReasoningEffort())) {
            builder.reasoningEffort(properties.getReasoningEffort());
        }

        return builder.build();
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return null;
        }
        return response.getResult().getOutput().getText();
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
