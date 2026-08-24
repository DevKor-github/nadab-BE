package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerConversationMessage;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPrompt;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.global.core.prompt.askchat.AskChatAnswerPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AskChatAnswerPromptComposer implements AskChatAnswerPromptAugmenter {

    private static final String NO_REFERENCE_DOCUMENT = "검색된 사용자 기록이 없습니다.";
    private static final String NO_RECENT_MESSAGE = "최근 대화가 없습니다.";
    private static final Pattern PROMPT_VERSION_SECTION_PATTERN = Pattern.compile(
            "(?m)^\\[프롬프트 버전]\\R\\{promptVersion}(?:\\R){1,2}"
    );
    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile(
            "\\{([A-Za-z][A-Za-z0-9_]*)}"
    );

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
        String template = PROMPT_VERSION_SECTION_PATTERN
                .matcher(promptLoader.loadUserPrompt())
                .replaceFirst("");

        return renderTemplate(template, context);
    }

    private String renderTemplate(String template, AskChatAnswerPromptContext context) {
        String question = escapePromptData(context.question());
        String recentMessages = formatRecentMessages(context.recentMessages());
        String referenceDocuments = formatReferenceDocuments(context.referenceDocuments());
        String followUpQuestionCount = String.valueOf(properties.getFollowUpQuestionCount());

        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(template);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String replacement = switch (matcher.group(1)) {
                case "question" -> question;
                case "recentMessages" -> recentMessages;
                case "referenceDocuments" -> referenceDocuments;
                case "followUpQuestionCount" -> followUpQuestionCount;
                default -> throw new AiServiceException(ErrorCode.PROMPT_ASK_CHAT_VARIABLE_UNSUPPORTED);
            };
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
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
                    .append(escapePromptData(message.content()))
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
            builder.append("[기록 ")
                    .append(i + 1)
                    .append("]")
                    .append(System.lineSeparator())
                    .append(escapePromptData(document.content()))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private String escapePromptData(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
