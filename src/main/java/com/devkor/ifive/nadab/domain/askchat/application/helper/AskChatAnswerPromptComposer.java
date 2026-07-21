package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerConversationMessage;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPrompt;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.global.core.prompt.askchat.AskChatAnswerPromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AskChatAnswerPromptComposer implements AskChatAnswerPromptAugmenter {

    private static final String NO_REFERENCE_DOCUMENT = "검색된 사용자 기록이 없습니다.";
    private static final String NO_RECENT_MESSAGE = "최근 대화가 없습니다.";

    private final AskChatAnswerProperties properties;
    private final AskChatAnswerPromptLoader promptLoader;

    @Override
    public AskChatAnswerPrompt augment(AskChatAnswerPromptContext context) {
        return new AskChatAnswerPrompt(
                systemPrompt(),
                userPrompt(context)
        );
    }

    private String systemPrompt() {
        return promptLoader.loadSystemPrompt();
    }

    private String userPrompt(AskChatAnswerPromptContext context) {
        return promptLoader.loadUserPrompt()
                .replace("{promptVersion}", String.valueOf(properties.getPromptVersion()))
                .replace("{question}", context.question())
                .replace("{recentMessages}", formatRecentMessages(context.recentMessages()))
                .replace("{referenceDocuments}", formatReferenceDocuments(context.referenceDocuments()))
                .replace("{followUpQuestionCount}", String.valueOf(properties.getFollowUpQuestionCount()));
    }

    private String formatRecentMessages(List<AskChatAnswerConversationMessage> messages) {
        if (messages.isEmpty()) {
            return NO_RECENT_MESSAGE;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            AskChatAnswerConversationMessage message = messages.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(message.role())
                    .append(": ")
                    .append(message.content())
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private String formatReferenceDocuments(List<AskChatAnswerReferenceDocument> documents) {
        if (documents.isEmpty()) {
            return NO_REFERENCE_DOCUMENT;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            AskChatAnswerReferenceDocument document = documents.get(i);
            builder.append(i + 1)
                    .append(". documentId=")
                    .append(document.documentId())
                    .append(", sourceType=")
                    .append(document.sourceType())
                    .append(", interestCode=")
                    .append(document.interestCode())
                    .append(", distance=")
                    .append(document.distance())
                    .append(System.lineSeparator())
                    .append(document.content())
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }
}
