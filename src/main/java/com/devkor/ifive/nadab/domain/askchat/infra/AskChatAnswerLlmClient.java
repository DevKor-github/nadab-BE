package com.devkor.ifive.nadab.domain.askchat.infra;

import com.devkor.ifive.nadab.domain.askchat.application.helper.AskChatAnswerPromptAugmenter;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerGenerationResult;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPrompt;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatGeneratedAnswer;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmExceptionMapper;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsageExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AskChatAnswerLlmClient {

    /*
     * Keep prompt augmentation behind this boundary while evidence documents are stored in
     * ask_chat_message_references. A future Spring AI Advisor implementation can replace this
     * collaborator without changing the ChatClient call path.
     */
    private final AskChatAnswerPromptAugmenter promptAugmenter;
    private final AskChatAnswerProperties properties;
    private final LlmRouter llmRouter;
    private final ObjectMapper objectMapper;

    public AskChatAnswerGenerationResult generate(AskChatAnswerPromptContext context) {
        AskChatAnswerPrompt prompt = promptAugmenter.augment(context);
        LlmProvider provider = properties.getProvider();
        ChatClient client = llmRouter.route(provider);

        ChatResponse response;
        try {
            response = client.prompt()
                    .system(prompt.systemPrompt())
                    .user(prompt.userPrompt())
                    .options(options())
                    .call()
                    .chatResponse();
        } catch (Exception e) {
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        }

        String content = extractContent(response);
        if (content == null || content.isBlank()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        AskChatGeneratedAnswer answer = parseAnswer(content);
        validateAnswer(answer);

        return new AskChatAnswerGenerationResult(
                answer,
                provider,
                properties.getModel(),
                LlmTokenUsageExtractor.extract(response),
                referenceDocumentIds(context)
        );
    }

    private OpenAiChatOptions options() {
        return OpenAiChatOptions.builder()
                .model(properties.getModel())
                .reasoningEffort(properties.getReasoningEffort())
                .temperature(properties.getTemperature())
                .maxCompletionTokens(properties.getMaxTokens())
                .build();
    }

    private AskChatGeneratedAnswer parseAnswer(String content) {
        try {
            return objectMapper.readValue(content, AskChatGeneratedAnswer.class);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private void validateAnswer(AskChatGeneratedAnswer answer) {
        if (answer == null || isBlank(answer.answer())) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
        }

        if (answer.followUpQuestions().size() > properties.getFollowUpQuestionCount()) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
        }

        for (String followUpQuestion : answer.followUpQuestions()) {
            if (isBlank(followUpQuestion)) {
                throw new AiResponseParseException(ErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }
        }
    }

    private List<Long> referenceDocumentIds(AskChatAnswerPromptContext context) {
        return context.referenceDocuments().stream()
                .map(AskChatAnswerReferenceDocument::documentId)
                .toList();
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return null;
        }
        return response.getResult().getOutput().getText();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
